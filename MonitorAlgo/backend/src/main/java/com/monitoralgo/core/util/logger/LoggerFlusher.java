package com.monitoralgo.core.util.logger;

import java.io.Flushable;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Flusher global : un seul scheduler daemon qui flush tous les writers
 * une fois par seconde. Évite un thread par logger et les fuites.
 */
final class LoggerFlusher {
    private static final LoggerFlusher INSTANCE = new LoggerFlusher();

    private final ScheduledExecutorService ses;
    private final Set<Flushable> toFlush = ConcurrentHashMap.newKeySet();

    private LoggerFlusher() {
        this.ses = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "logger-flusher");
            t.setDaemon(true);
            return t;
        });
        ses.scheduleAtFixedRate(() -> {
            for (Flushable f : toFlush) {
                try {
                    f.flush();
                } catch (Exception ignore) {
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    static LoggerFlusher getInstance() {
        return INSTANCE;
    }

    void register(Flushable f) {
        toFlush.add(f);
    }

    void unregister(Flushable f) {
        toFlush.remove(f);
    }
}
