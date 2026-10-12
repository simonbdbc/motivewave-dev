package com.monitoralgo.core.util.logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.monitoralgo.config.GlobalConfig;

/**
 * Logger pour "studies" avec un fichier PARTAGÉ par studyId + rotation 00h/12h.
 * Multi-thread safe, ref-count : fermeture du fichier seulement quand le
 * DERNIER
 * utilisateur le libère. Écritures sous lock, rotation atomique.
 */
public final class RotatingLogger implements Closeable {

    private final SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat dfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private final SimpleDateFormat dfHour = new SimpleDateFormat("HH");

    private final String studyId;
    private final String instanceId;
    private final SharedWriter shared;

    // Déduplication par instance
    private String lastMsg = null;
    private int lastCount = 0;
    private String lastTs = null;
    private String lastContext = null;

    private static final Registry REGISTRY = new Registry();
    private static final LoggerFlusher FLUSHER = LoggerFlusher.getInstance();

    // ==== API ====

    public static RotatingLogger get(String studyId, String instanceId) {
        return new RotatingLogger(studyId, instanceId, REGISTRY.acquire(studyId));
    }

    private RotatingLogger(String studyId, String instanceId, SharedWriter shared) {
        this.studyId = Objects.requireNonNull(studyId);
        this.instanceId = Objects.requireNonNull(instanceId);
        this.shared = Objects.requireNonNull(shared);
        FLUSHER.register(shared);
    }

    public void setContext(String... fields) {
        String ctx = String.join(" | ", fields);
        if (!Objects.equals(ctx, lastContext)) {
            flushLastDuplicate();
            String now = dfTime.format(new Date());
            shared.writeLocked(w -> {
                w.ensureOpenAndHeader(this::computeHalfKey);
                w.bw.write(now + " | " + instanceId + " | [CONTEXT] | " + ctx);
                w.bw.newLine();
            });
            lastContext = ctx;
        }
    }

    public void log(String message) {
        String now = dfTime.format(new Date());
        if (Objects.equals(message, lastMsg)) {
            lastCount++;
        } else {
            flushLastDuplicate();
            String thread = Thread.currentThread().getName();
            shared.writeLocked(w -> {
                w.ensureOpenAndHeader(this::computeHalfKey);
                w.bw.write(now + " | " + instanceId + " | [" + thread + "] | " + message);
                w.bw.newLine();
            });
            lastMsg = message;
            lastTs = now;
            lastCount = 1;
        }
    }

    @Override
    public void close() {
        flushLastDuplicate();
        FLUSHER.unregister(shared);
        REGISTRY.release(studyId);
    }

    private void flushLastDuplicate() {
        if (lastMsg != null && lastCount > 1) {
            String thread = Thread.currentThread().getName();
            final String ts = lastTs;
            final String msg = lastMsg;
            final int cnt = lastCount;
            shared.writeLocked(w -> {
                w.ensureOpenAndHeader(this::computeHalfKey);
                w.bw.write(ts + " | " + instanceId + " | [" + thread + "] | " + msg + " (x " + cnt + ")");
                w.bw.newLine();
            });
        }
        lastMsg = null;
        lastCount = 0;
        lastTs = null;
    }

    private HalfKey computeHalfKey() {
        Date now = new Date();
        String date = dfDate.format(now);
        String half = Integer.parseInt(dfHour.format(now)) < 12 ? "00" : "12";
        return new HalfKey(date, half);
    }

    // ==== Internals (shared writer) ====

    private static final class Registry {
        private final Map<String, SharedWriter> map = new ConcurrentHashMap<>();

        SharedWriter acquire(String studyId) {
            return map.compute(studyId, (k, v) -> {
                if (v == null)
                    v = new SharedWriter(k);
                v.incRef();
                return v;
            });
        }

        void release(String studyId) {
            map.computeIfPresent(studyId, (k, v) -> {
                if (v.decRef() == 0) {
                    v.closeQuietly();
                    return null;
                }
                return v;
            });
        }
    }

    private static final class HalfKey {
        final String date;
        final String half;

        HalfKey(String d, String h) {
            this.date = d;
            this.half = h;
        }
    }

    /** Writer partagé (un par studyId) avec rotation 00/12 et header unique. */
    private static final class SharedWriter implements Flushable {
        private final String studyId;
        private final ReentrantLock lock = new ReentrantLock(true);

        private BufferedWriter bw;
        private String currentDate;
        private String currentHalf;
        private boolean headerWritten = false;
        private boolean closed = false;
        private int refCount = 0;

        SharedWriter(String studyId) {
            this.studyId = studyId;
        }

        void incRef() {
            lock.lock();
            try {
                if (closed)
                    throw new IllegalStateException("Closed writer for " + studyId);
                if (bw == null)
                    openFile(initialHalfKey());
                refCount++;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                lock.unlock();
            }
        }

        int decRef() {
            lock.lock();
            try {
                return --refCount;
            } finally {
                lock.unlock();
            }
        }

        void writeLocked(IOConsumer<SharedWriter> task) {
            lock.lock();
            try {
                if (!closed)
                    task.accept(this);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } finally {
                lock.unlock();
            }
        }

        void ensureOpenAndHeader(SupplierEx<HalfKey> keySupplier) throws IOException {
            HalfKey key = keySupplier.get();
            if (bw == null || !Objects.equals(key.date, currentDate) || !Objects.equals(key.half, currentHalf)) {
                rotateTo(key);
            }
            if (!headerWritten) {
                bw.write(
                        "---------------------------------------------------------------------------------------------------");
                bw.newLine();
                bw.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + " | " + studyId);
                bw.newLine();
                bw.write(
                        "---------------------------------------------------------------------------------------------------");
                bw.newLine();
                headerWritten = true;
            }
        }

        private void rotateTo(HalfKey key) throws IOException {
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
            openFile(key);
        }

        private HalfKey initialHalfKey() {
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String half = Integer.parseInt(new SimpleDateFormat("HH").format(new Date())) < 12 ? "00" : "12";
            return new HalfKey(date, half);
        }

        private void openFile(HalfKey key) throws IOException {
            File dir = new File(GlobalConfig.get("ALGO_LOG_FOLDER") + "/" + key.date);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(dir, studyId + "_" + key.half + ".log");
            this.bw = new BufferedWriter(new FileWriter(file, true));
            this.currentDate = key.date;
            this.currentHalf = key.half;
            this.headerWritten = false;
        }

        @Override
        public void flush() {
            lock.lock();
            try {
                if (!closed && bw != null)
                    bw.flush();
            } catch (IOException ignore) {
            } finally {
                lock.unlock();
            }
        }

        void closeQuietly() {
            lock.lock();
            try {
                if (!closed) {
                    closed = true;
                    if (bw != null) {
                        try {
                            bw.flush();
                        } catch (IOException ignore) {
                        }
                        try {
                            bw.close();
                        } catch (IOException ignore) {
                        }
                        bw = null;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    @FunctionalInterface
    private interface IOConsumer<T> {
        void accept(T t) throws IOException;
    }

    @FunctionalInterface
    private interface SupplierEx<T> {
        T get() throws IOException;
    }
}
