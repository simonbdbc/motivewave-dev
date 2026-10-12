package com.monitoralgo.core.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.monitoralgo.config.GlobalConfig;

public class CoreLogger implements Closeable {
    private static final Object LOCK = new Object();
    private static final Map<String, CoreLogger> instances = new HashMap<>();

    /** Liste des studyId qui doivent être en mode "single file, no rotation" */
    private static final Set<String> SINGLE_FILE_IDS = new HashSet<>(Arrays.asList(
            "ALGO" // Ajoute ici ce que tu veux
    ));

    public static void addSingleFileId(String id) {
        SINGLE_FILE_IDS.add(id);
    }

    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private final SimpleDateFormat minuteFormat = new SimpleDateFormat("mm");
    private final SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final String studyId;
    private final String instanceId;
    private String launchTime = null;
    private String currentHalfDay = null;
    private String currentDate;
    private BufferedWriter bw;
    private boolean headerWritten = false;
    private Thread flushThread;
    private volatile boolean running = true;

    private String lastLogMessage = null;
    private int lastLogCount = 0;
    private String lastLogTimestamp = null;
    private String lastContext = null;

    public static CoreLogger getInstance(String studyId, String instanceId) {
        String key = isSingleFileMode(studyId) ? studyId : (studyId + "_" + instanceId);
        synchronized (LOCK) {
            return instances.computeIfAbsent(key, k -> new CoreLogger(studyId, instanceId));
        }
    }

    private static boolean isSingleFileMode(String id) {
        return SINGLE_FILE_IDS.contains(id);
    }

    private CoreLogger(String studyId, String instanceId) {
        this.studyId = studyId;
        this.instanceId = instanceId;
        updateDate();
        if (isSingleFileMode(studyId)) {
            this.launchTime = hourFormat.format(new Date()) + "-" + minuteFormat.format(new Date()) + "-"
                    + secondFormat.format(new Date());
            createSingleFileLog();
        } else {
            this.currentHalfDay = getHalfDayKey();
            createRotatingLogFile();
        }
        startFlushThread();
    }

    private String getHalfDayKey() {
        int hour = Integer.parseInt(hourFormat.format(new Date()));
        return (hour < 12) ? "00" : "12";
    }

    private void updateDate() {
        this.currentDate = dateFormat.format(new Date());
    }

    private File getLogsDir() {
        String rootPath = GlobalConfig.get("ALGO_LOG_FOLDER");
        File logsDir = new File(rootPath + "/" + currentDate);
        if (!logsDir.exists())
            logsDir.mkdirs();
        return logsDir;
    }

    private void createSingleFileLog() {
        try {
            File logsDir = getLogsDir();
            String fileName = studyId + "_" + launchTime + ".log";
            this.bw = new BufferedWriter(new FileWriter(new File(logsDir, fileName), true));
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir le fichier de log", e);
        }
    }

    private void createRotatingLogFile() {
        try {
            File logsDir = getLogsDir();
            String fileName = studyId + "_" + currentHalfDay + ".log";
            this.bw = new BufferedWriter(new FileWriter(new File(logsDir, fileName), true));
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir le fichier de log", e);
        }
    }

    private void rotateFileIfNeeded() throws IOException {
        updateDate();
        if (isSingleFileMode(studyId)) {
            return;
        }
        String newHalfDay = getHalfDayKey();
        String newDate = dateFormat.format(new Date());
        if (!newHalfDay.equals(currentHalfDay) || !newDate.equals(currentDate)) {
            flushLastLogMessage();
            bw.flush();
            bw.close();
            headerWritten = false;
            currentHalfDay = newHalfDay;
            currentDate = newDate;
            createRotatingLogFile();
        }
    }

    private void startFlushThread() {
        running = true;
        flushThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    synchronized (this) {
                        if (bw != null)
                            bw.flush();
                    }
                } catch (Exception ignore) {
                }
            }
        });
        flushThread.setDaemon(true);
        flushThread.start();
    }

    private void writeHeaderIfNeeded() throws IOException {
        if (!headerWritten) {
            bw.write(
                    "--------------------------------------------------------------------------------------------------- \n");
            bw.write(datetimeFormat.format(new Date()) + " | " + instanceId + " | " + studyId + "\n");
            bw.write(
                    "--------------------------------------------------------------------------------------------------- \n");
            headerWritten = true;
        }
    }

    public synchronized void setContext(String... contextFields) {
        try {
            rotateFileIfNeeded();
            writeHeaderIfNeeded();
            String context = String.join(" | ", contextFields);
            if (!equals(lastContext, context)) {
                flushLastLogMessage();
                bw.write(datetimeFormat.format(new Date()) + " | " + instanceId + " | [CONTEXT] | " + context + "\n");
                lastContext = context;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void log(String message) {
        try {
            rotateFileIfNeeded();
            writeHeaderIfNeeded();
            String now = datetimeFormat.format(new Date());
            String threadName = Thread.currentThread().getName();

            if (lastLogMessage != null && lastLogMessage.equals(message)) {
                lastLogCount++;
            } else {
                flushLastLogMessage();
                if (isSingleFileMode(studyId)) {
                    bw.write(now + " | " + instanceId + " | [" + threadName + "] | " + message + "\n");
                } else {
                    bw.write(now + " | " + instanceId + " | " + message + "\n");
                }
                lastLogMessage = message;
                lastLogCount = 1;
                lastLogTimestamp = now;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushLastLogMessage() throws IOException {
        if (lastLogMessage != null && lastLogCount > 1) {
            if (isSingleFileMode(studyId)) {
                String threadName = Thread.currentThread().getName();
                bw.write(lastLogTimestamp + " | " + instanceId + " | [" + threadName + "] | " + lastLogMessage + " (x "
                        + lastLogCount + ")\n");
            } else {
                bw.write(
                        lastLogTimestamp + " | " + instanceId + " | " + lastLogMessage + " (x " + lastLogCount + ")\n");
            }
        }
        lastLogMessage = null;
        lastLogCount = 0;
        lastLogTimestamp = null;
    }

    @Override
    public synchronized void close() {
        running = false;
        try {
            if (flushThread != null)
                flushThread.join(2000);
            flushLastLogMessage();
            if (bw != null) {
                bw.flush();
                bw.close();
            }
        } catch (IOException | InterruptedException e) {
            // Rien à faire
        }
    }

    private boolean equals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
