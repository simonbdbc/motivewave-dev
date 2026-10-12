package sbdev.joy_ai_project.strategy.util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StudyLogger implements Closeable {
    private static final Object LOCK = new Object();
    private static final Map<String, StudyLogger> instances = new HashMap<>();
    private final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("HH");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final String studyId;
    private final String instanceId;
    private String currentHour;
    private String currentDate;
    private BufferedWriter bw;
    private boolean headerWritten = false;
    private Thread flushThread;
    private volatile boolean running = true;

    // Déduplication
    private String lastLogMessage = null;
    private int lastLogCount = 0;
    private String lastLogTimestamp = null;

    private String lastInstrument = null;
    private String lastInterval = null;

    public static StudyLogger getInstance(String studyId, String instanceId) {
        String key = studyId + "_" + instanceId;
        synchronized (LOCK) {
            return instances.computeIfAbsent(key, _ -> new StudyLogger(studyId, instanceId));
        }
    }

    private StudyLogger(String studyId, String instanceId) {
        this.studyId = studyId;
        this.instanceId = instanceId;
        updateDateAndHour();
        createOrOpenLogFile();
        startFlushThread();
    }

    /** Appelée à chaque log : vérifie l’heure, change de fichier si besoin */
    private void rotateFileIfHourChanged() throws IOException {
        String newHour = hourFormat.format(new Date());
        String newDate = dateFormat.format(new Date());
        if (!newHour.equals(currentHour) || !newDate.equals(currentDate)) {
            // On flush le dernier message groupé dans l'ancien fichier
            flushLastLogMessage();
            bw.flush();
            bw.close();
            headerWritten = false;
            currentHour = newHour;
            currentDate = newDate;
            createOrOpenLogFile();
        }
    }

    private void createOrOpenLogFile() {
        try {
            String userHome = System.getProperty("user.home");
            File logsDir = new File(userHome, "MotiveWaveLogs" + File.separator + currentDate);
            if (!logsDir.exists())
                logsDir.mkdirs();
            String fileName = studyId + "_" + currentHour + ".log";
            this.bw = new BufferedWriter(new FileWriter(new File(logsDir, fileName), true));
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'ouvrir le fichier de log", e);
        }
    }

    private void updateDateAndHour() {
        Date now = new Date();
        this.currentHour = hourFormat.format(now);
        this.currentDate = dateFormat.format(now);
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

    public synchronized void setContext(String instrument, String interval) {
        try {
            rotateFileIfHourChanged();
            writeHeaderIfNeeded();
            if (!equals(lastInstrument, instrument) || !equals(lastInterval, interval)) {
                flushLastLogMessage();
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
            rotateFileIfHourChanged();
            writeHeaderIfNeeded();
            String now = datetimeFormat.format(new Date());

            if (lastLogMessage != null && lastLogMessage.equals(message)) {
                lastLogCount++;
            } else {
                flushLastLogMessage();
                bw.write(now + " | " + instanceId + " | " + message + "\n");
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
            bw.write(lastLogTimestamp + " | " + instanceId + " | " + lastLogMessage + " (x " + lastLogCount + ")\n");
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
