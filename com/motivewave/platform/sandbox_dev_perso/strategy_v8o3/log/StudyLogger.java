package sbdev.joy_ai_project.strategy_v8o3.log;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudyLogger implements Closeable {
    private static final Object LOCK = new Object();
    private static final Map<String, StudyLogger> instances = new HashMap<>();
    private final BufferedWriter bw;
    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final String studyId;
    private final String instanceId;
    private final String launchTime;
    private final String dateDir;
    private String lastInstrument = null;
    private String lastInterval = null;
    private boolean headerWritten = false;
    private final Thread flushThread;
    private volatile boolean running = true;

    public static StudyLogger getInstance(String studyId, String instanceId) {
        String key = studyId + "_" + instanceId;
        synchronized (LOCK) {
            return instances.computeIfAbsent(key, _ -> new StudyLogger(studyId, instanceId));
        }
    }

    private StudyLogger(String studyId, String instanceId) {
        this.studyId = studyId;
        this.instanceId = instanceId;
        this.launchTime = new SimpleDateFormat("HH").format(new Date());
        this.dateDir = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String userHome = System.getProperty("user.home");
        // Ajout du dossier par jour
        File logsDir = new File(userHome, "MotiveWaveLogs" + File.separator + dateDir);
        if (!logsDir.exists())
            logsDir.mkdirs();
        // String fileName = studyId + "_" + launchTime + "_" + instanceId + ".log";
        String fileName = studyId + "_" + launchTime + ".log";
        try {
            this.bw = new BufferedWriter(new FileWriter(new File(logsDir, fileName), true));
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir le fichier de log", e);
        }

        // Flush périodique (optionnel)
        flushThread = new Thread(() -> {
            while (running) {
                try {
                    Thread.sleep(1000);
                    synchronized (this) {
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
            bw.write(datetimeFormat.format(new Date()) + " | " + instanceId + "| " + studyId + "\n");
            bw.write(
                    "--------------------------------------------------------------------------------------------------- \n");
            headerWritten = true;
        }
    }

    public synchronized void setContext(String instrument, String interval) {
        try {
            writeHeaderIfNeeded();
            if (!equals(lastInstrument, instrument) || !equals(lastInterval, interval)) {
                bw.write(datetimeFormat.format(new Date()) + " | " + instanceId + " | Instrument: " + instrument
                        + " | Interval: " + interval + "\n");
                lastInstrument = instrument;
                lastInterval = interval;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void log(String message) {
        try {
            writeHeaderIfNeeded();
            bw.write(datetimeFormat.format(new Date()) + " | " + instanceId + " | " + message + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        running = false;
        try {
            flushThread.join(2000);
            bw.flush();
            bw.close();
        } catch (IOException | InterruptedException e) {
            // Rien à faire
        }
    }

    private boolean equals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
