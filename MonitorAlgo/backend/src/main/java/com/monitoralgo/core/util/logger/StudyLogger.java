package com.monitoralgo.core.util.logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.monitoralgo.config.GlobalConfig;

/**
 * Logger "par instance" avec rotation HORAIRE (path utilisateur).
 * - 1 fichier par (studyId, instanceId) et par heure
 * - Écritures thread-safe (synchronized)
 * - Plus de thread de flush dédié : on utilise LoggerFlusher global
 * - Fermeture idempotente
 *
 * Chemin : ${user.home}/MotiveWaveLogs/yyyy-MM-dd/{studyId}_{HH}.log
 */
public class StudyLogger implements Closeable, Flushable {

    private static final Object LOCK = new Object();
    private static final Map<String, StudyLogger> INSTANCES = new HashMap<>();

    private final SimpleDateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat dfHour = new SimpleDateFormat("HH");
    private final SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");

    private final String studyId;
    private final String instanceId;

    private String currentHour;
    private String currentDate;
    private BufferedWriter bw;
    private boolean headerWritten = false;
    private boolean closed = false;

    // Déduplication
    private String lastLogMessage = null;
    private int lastLogCount = 0;
    private String lastLogTimestamp = null;

    private String lastInstrument = null;
    private String lastInterval = null;

    private static final LoggerFlusher FLUSHER = LoggerFlusher.getInstance();

    public static StudyLogger get(String studyId, String instanceId) {
        String key = studyId + "_" + instanceId;
        synchronized (LOCK) {
            StudyLogger l = INSTANCES.get(key);
            if (l == null || l.closed) {
                l = new StudyLogger(studyId, instanceId);
                INSTANCES.put(key, l);
            }
            return l;
        }
    }

    private StudyLogger(String studyId, String instanceId) {
        this.studyId = Objects.requireNonNull(studyId);
        this.instanceId = Objects.requireNonNull(instanceId);
        updateDateAndHour();
        createOrOpenLogFile();
        FLUSHER.register(this);
    }

    private synchronized void rotateFileIfHourChanged() throws IOException {
        String newHour = dfHour.format(new Date());
        String newDate = dfDate.format(new Date());
        if (!newHour.equals(currentHour) || !newDate.equals(currentDate)) {
            flushLastLogMessageLocked();
            if (bw != null) {
                try {
                    bw.flush();
                } catch (IOException ignore) {
                }
                try {
                    bw.close();
                } catch (IOException ignore) {
                }
            }
            headerWritten = false;
            currentHour = newHour;
            currentDate = newDate;
            createOrOpenLogFile();
        }
    }

    private void createOrOpenLogFile() {
        try {
            String userHome = GlobalConfig.get("MOTIVEWAVE_LOG_FOLDER");
            File logsDir = new File(userHome, "/" + File.separator + currentDate);
            if (!logsDir.exists())
                logsDir.mkdirs();
            String fileName = studyId + "_" + currentHour + ".log";
            this.bw = new BufferedWriter(new FileWriter(new File(logsDir, fileName), true));
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'ouvrir le fichier de log", e);
        }
    }

    private void updateDateAndHour() {
        Date now = new Date();
        this.currentHour = dfHour.format(now);
        this.currentDate = dfDate.format(now);
    }

    private void writeHeaderIfNeeded() throws IOException {
        if (!headerWritten) {
            bw.write(
                    "---------------------------------------------------------------------------------------------------");
            bw.newLine();
            bw.write(dfTime.format(new Date()) + " | " + instanceId + " | " + studyId);
            bw.newLine();
            bw.write(
                    "---------------------------------------------------------------------------------------------------");
            bw.newLine();
            headerWritten = true;
        }
    }

    /** Contexte spécifique (instrument, interval). */
    public synchronized void setContext(String instrument, String interval) {
        try {
            rotateFileIfHourChanged();
            writeHeaderIfNeeded();
            if (!equals(lastInstrument, instrument) || !equals(lastInterval, interval)) {
                flushLastLogMessageLocked();
                bw.write(dfTime.format(new Date()) + " | " + instanceId + " | Instrument: " + instrument
                        + " | Interval: " + interval);
                bw.newLine();
                lastInstrument = instrument;
                lastInterval = interval;
            }
        } catch (IOException e) {
            // pas d'exception checked côté appelant
        }
    }

    /** Contexte générique (optionnel) pour aligner avec les autres loggers. */
    public synchronized void setContext(String... fields) {
        String ctx = String.join(" | ", fields);
        try {
            rotateFileIfHourChanged();
            writeHeaderIfNeeded();
            flushLastLogMessageLocked();
            bw.write(dfTime.format(new Date()) + " | " + instanceId + " | [CONTEXT] | " + ctx);
            bw.newLine();
        } catch (IOException e) {
            // ignore
        }
    }

    public synchronized void log(String message) {
        try {
            rotateFileIfHourChanged();
            writeHeaderIfNeeded();
            String now = dfTime.format(new Date());
            if (lastLogMessage != null && lastLogMessage.equals(message)) {
                lastLogCount++;
            } else {
                flushLastLogMessageLocked();
                bw.write(now + " | " + instanceId + " | " + message);
                bw.newLine();
                lastLogMessage = message;
                lastLogCount = 1;
                lastLogTimestamp = now;
            }
        } catch (IOException e) {
            // ignore
        }
    }

    private void flushLastLogMessageLocked() throws IOException {
        if (lastLogMessage != null && lastLogCount > 1) {
            bw.write(lastLogTimestamp + " | " + instanceId + " | " + lastLogMessage + " (x " + lastLogCount + ")");
            bw.newLine();
        }
        lastLogMessage = null;
        lastLogCount = 0;
        lastLogTimestamp = null;
    }

    @Override
    public synchronized void flush() {
        if (!closed && bw != null) {
            try {
                bw.flush();
            } catch (IOException ignore) {
            }
        }
    }

    @Override
    public synchronized void close() {
        if (closed)
            return;
        closed = true;
        FLUSHER.unregister(this);
        try {
            flushLastLogMessageLocked();
        } catch (IOException ignore) {
        }
        try {
            if (bw != null)
                bw.flush();
        } catch (IOException ignore) {
        }
        try {
            if (bw != null)
                bw.close();
        } catch (IOException ignore) {
        }
        bw = null;
        // Retire de la map statique
        synchronized (LOCK) {
            INSTANCES.values().removeIf(l -> l == this);
        }
    }

    private static boolean equals(String a, String b) {
        return (a == null && b == null) || (a != null && a.equals(b));
    }
}
