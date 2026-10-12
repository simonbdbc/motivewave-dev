package com.monitoralgo.core.util.logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.monitoralgo.config.GlobalConfig;

/**
 * Logger pour jobs/process lancés depuis le front :
 * - 1 fichier par EXÉCUTION (processName + processId + runStamp)
 * - Pas de rotation
 * - Écritures protégées par lock
 * - Fermeture idempotente
 */
public final class ProcessLogger implements Closeable, Flushable {

    private static final SimpleDateFormat DF_DATE = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DF_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SimpleDateFormat DF_RUNSTAMP = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS");
    private static final SimpleDateFormat DF_RUNSTAMP_FILE = new SimpleDateFormat("HH-mm-ss-SSS");

    private final String processName; // ex: ALGO
    private final String processId; // ex: UUID fourni ou métier UPDATE-STATS
    private final String runStamp; // ex: 2025_08_10-07_22_31_153 -> garantit 1 fichier/exécution
    private final String runId; // processId + "_" + runStamp (utile pour le front)
    private final String runStampFile; // ex: 07-22-31-153 -> garantit 1 fichier/exécution dans le dossier du jour
    private final File logFile; // fichier unique de cette exécution

    private final Object lock = new Object();
    private volatile BufferedWriter bw;
    private volatile boolean headerWritten = false;
    private volatile boolean closed = false;

    // Déduplication
    private String lastMsg = null;
    private int lastCount = 0;
    private String lastTs = null;
    private String lastContext = null;

    private static final LoggerFlusher FLUSHER = LoggerFlusher.getInstance();

    public static ProcessLogger open(String processName, String processId) {
        return new ProcessLogger(processName, processId);
    }

    private ProcessLogger(String processName, String processId) {
        this.processName = Objects.requireNonNull(processName, "processName");
        this.processId = Objects.requireNonNull(processId, "processId");
        this.runStamp = DF_RUNSTAMP.format(new Date());
        this.runStampFile = DF_RUNSTAMP_FILE.format(new Date());
        this.runId = this.processId + "_" + this.runStamp;

        String date = DF_DATE.format(new Date());
        File dir = new File(GlobalConfig.get("ALGO_LOG_FOLDER") + "/" + date);
        if (!dir.exists())
            dir.mkdirs();
        this.logFile = new File(dir, processName + "_" + processId + "_" + runStampFile + ".log");

        openFile();
        FLUSHER.register(this);
    }

    /** Identifiant unique d’exécution (à renvoyer au front si besoin). */
    public String getRunId() {
        return runId;
    }

    /** Emplacement du fichier de log de cette exécution. */
    public File getLogFile() {
        return logFile;
    }

    private void openFile() {
        synchronized (lock) {
            if (closed)
                throw new IllegalStateException("Logger already closed");
            try {
                this.bw = new BufferedWriter(new FileWriter(logFile, true));
                this.headerWritten = false;
                writeHeaderIfNeeded();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void writeHeaderIfNeeded() throws IOException {
        if (!headerWritten) {
            bw.write(
                    "---------------------------------------------------------------------------------------------------");
            bw.newLine();
            bw.write(DF_TIME.format(new Date()) + " | " + processName + " | " + processId + " | run=" + runStamp);
            bw.newLine();
            bw.write(
                    "---------------------------------------------------------------------------------------------------");
            bw.newLine();
            headerWritten = true;
        }
    }

    /** Ajoute/actualise un contexte textuel. */
    public void setContext(String... fields) {
        String ctx = String.join(" | ", fields);
        synchronized (lock) {
            if (closed)
                return;
            try {
                if (!Objects.equals(ctx, lastContext)) {
                    flushLastDuplicateLocked();
                    bw.write(DF_TIME.format(new Date()) + " | [CONTEXT] | " + ctx);
                    bw.newLine();
                    lastContext = ctx;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /** Log d’une ligne thread-safe. */
    public void log(String message) {
        String now = DF_TIME.format(new Date());
        synchronized (lock) {
            if (closed)
                return;
            try {
                if (Objects.equals(message, lastMsg)) {
                    lastCount++;
                } else {
                    flushLastDuplicateLocked();
                    String thread = Thread.currentThread().getName();
                    bw.write(now + " | [" + thread + "] | " + message);
                    bw.newLine();
                    lastMsg = message;
                    lastTs = now;
                    lastCount = 1;
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    private void flushLastDuplicateLocked() throws IOException {
        if (lastMsg != null && lastCount > 1) {
            String thread = Thread.currentThread().getName();
            bw.write(lastTs + " | [" + thread + "] | " + lastMsg + " (x " + lastCount + ")");
            bw.newLine();
        }
        lastMsg = null;
        lastCount = 0;
        lastTs = null;
    }

    @Override
    public void flush() {
        synchronized (lock) {
            if (!closed && bw != null)
                try {
                    bw.flush();
                } catch (IOException ignore) {
                }
        }
    }

    @Override
    public void close() {
        // Idempotent – ne touche que CE fichier d’exécution
        FLUSHER.unregister(this);
        synchronized (lock) {
            if (closed)
                return;
            closed = true;
            try {
                flushLastDuplicateLocked();
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
        }
    }
}
