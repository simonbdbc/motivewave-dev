package com.monitoralgo.monitoring.websocket;

// import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.monitoralgo.config.GlobalConfig;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * WebSocket "tail -f" sécurisé et idempotent pour lire des fichiers de logs
 * côté serveur.
 *
 * FONCTIONS CLÉS
 * - Sécurité de chemin: empêche l’access hors des racines autorisées.
 * - White-list de noms: seuls certains fichiers/ motifs sont lisibles.
 * - Démarrage "late tail": n’envoie que les ~300 Ko de fin de fichier puis
 * streame les nouveautés.
 * - Anti-doublon: 1 connexion active par (source,path). Une nouvelle connexion
 * ferme l’ancienne.
 * - Tolérance aux rotations/truncations: si le fichier rétrécit, on repart du
 * début.
 * - Nettoyage: enlève la session du registre en fermeture/erreur.
 */
@Component
public class LogFileWebSocketHandler extends TextWebSocketHandler {

    // @Value("${log.watcher.strategy-folder:logs/strategy}")
    // private String strategyRoot;

    // @Value("${log.watcher.motivewave-folder:logs/motivewave}")
    // private String motivewaveRoot;

    // // Racine "algo" = chemin local du projet (non configurable)
    // private final Path algoRoot =
    // Paths.get("${log.watcher.algo-folder:logs/algo}");

    private String strategyRoot = GlobalConfig.get("STRATEGY_LOG_FOLDER");
    private String motivewaveRoot = GlobalConfig.get("MOTIVEWAVE_LOG_FOLDER");
    private String algoRoot = GlobalConfig.get("ALGO_LOG_FOLDER");

    /** Registre global: clé = source + "|" + chemin absolu → session active */
    private final Map<String, WebSocketSession> liveByKey = new ConcurrentHashMap<>();

    /** Taille max envoyée au démarrage (late tail) */
    private static final long BOOTSTRAP_MAX_BYTES = 300L * 1024L;
    /** Période de poll du fichier (ms) */
    private static final long TAIL_POLL_MS = 1000L;

    public void setStrategyRoot(String s) {
        this.strategyRoot = s;
    }

    public void setMotivewaveRoot(String m) {
        this.motivewaveRoot = m;
    }

    @SuppressWarnings("null")
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        try {
            // ---- 1) Parse & validation des paramètres ----
            String query = session.getUri() != null ? session.getUri().getQuery() : null;
            if (query == null) {
                safeClose(session, CloseStatus.BAD_DATA);
                return;
            }

            Map<String, String> params = Arrays.stream(query.split("&"))
                    .map(kv -> kv.split("=", 2))
                    .filter(kv -> kv.length == 2)
                    .collect(Collectors.toMap(kv -> kv[0], kv -> URLDecoder.decode(kv[1], StandardCharsets.UTF_8)));

            String relPath = params.get("path");
            String source = params.get("source");
            if (relPath == null || source == null) {
                safeClose(session, CloseStatus.BAD_DATA);
                return;
            }

            // ---- 2) Sélection de la racine + whitelists par "source" ----
            Path root;
            boolean acceptAlgoBacktestLog = false;
            boolean acceptAlgoUpdateStatsLog = false;
            boolean acceptStrategyLog = false;
            boolean acceptOutputTxt = false;

            switch (source) {
                case "algo-backtest" -> {
                    // root = algoRoot;
                    root = Paths.get(algoRoot);
                    acceptAlgoBacktestLog = true;
                }
                case "algo-update-stats" -> {
                    // root = algoRoot;
                    root = Paths.get(algoRoot);
                    acceptAlgoUpdateStatsLog = true;
                }
                case "strategy" -> {
                    root = Paths.get(strategyRoot);
                    acceptStrategyLog = true;
                }
                case "motivewave" -> {
                    root = Paths.get(motivewaveRoot);
                    acceptOutputTxt = true;
                }
                default -> {
                    safeClose(session, CloseStatus.BAD_DATA);
                    return;
                }
            }

            // ---- 3) Résolution & sécurité de chemin ----
            Path file = root.resolve(relPath).normalize();
            if (!file.startsWith(root)) {
                safeClose(session, CloseStatus.NOT_ACCEPTABLE.withReason("Path outside root"));
                return;
            }
            if (!Files.exists(file) || Files.isDirectory(file)) {
                safeClose(session, CloseStatus.SESSION_NOT_RELIABLE);
                return;
            }

            // ---- 4) Whitelist sur le nom de fichier ----
            String fname = file.getFileName().toString();
            boolean isAlgoBacktestLog = acceptAlgoBacktestLog && fname.startsWith("ALGO_BACKTEST")
                    && fname.endsWith(".log");
            boolean isAlgoUpdateStatsLog = acceptAlgoUpdateStatsLog && fname.startsWith("ALGO_UPDATE-STATS")
                    && fname.endsWith(".log");
            boolean isStrategyLog = acceptStrategyLog && fname.endsWith(".log");
            boolean isOutputTxt = acceptOutputTxt && fname.startsWith("output") && fname.endsWith(".txt");

            if (!(isAlgoBacktestLog || isAlgoUpdateStatsLog || isStrategyLog || isOutputTxt)) {
                safeClose(session, CloseStatus.NOT_ACCEPTABLE.withReason("File not allowed"));
                return;
            }

            // ---- 5) Anti-doublon: 1 session active par (source,path) ----
            String key = source + "|" + file.toAbsolutePath();
            WebSocketSession old = liveByKey.put(key, session);
            if (old != null && old.isOpen()) {
                try {
                    old.close(CloseStatus.NORMAL.withReason("Replaced by newer session"));
                } catch (Exception ignore) {
                }
            }

            // On garde la clé pour le cleanup
            session.getAttributes().put("liveKey", key);

            // ---- 6) Lancement du tailer (thread daemon) ----
            Thread tailThread = new Thread(() -> runTailLoop(session, file, fname, key), "LogTail-" + fname);
            tailThread.setDaemon(true);
            session.getAttributes().put("tailThread", tailThread);
            tailThread.start();

            // (Optionnel) envoie un marqueur d’ouverture
            sendSafe(session, "---WS_OPEN---\n");

        } catch (Exception e) {
            System.err.println("LogFileWebSocketHandler establish error: " + e.getMessage());
            safeClose(session, CloseStatus.SERVER_ERROR);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Nettoyage du registre
        Object k = session.getAttributes().get("liveKey");
        if (k instanceof String key) {
            WebSocketSession cur = liveByKey.get(key);
            if (cur == session) {
                liveByKey.remove(key);
            }
        }
        // (Le thread s’arrête de lui-même car la boucle vérifie session.isOpen())
    }

    // ======================================================================
    // Implémentation du "tail" tolérant
    // ======================================================================
    private void runTailLoop(WebSocketSession session, Path file, String fname, String key) {
        long lastReadPos = 0L;

        try {
            // ---- Bootstrap: envoi des derniers ~300 Ko ----
            long size = Files.size(file);
            long startPos = Math.max(0, size - BOOTSTRAP_MAX_BYTES);
            streamFromOffset(session, file, startPos);
            lastReadPos = size;

            // ---- Boucle de tail ----
            while (session.isOpen()) {
                Thread.sleep(TAIL_POLL_MS);

                // Fichier peut être truncaté/rotaté → si la taille diminue, repartir au début
                long newSize;
                try {
                    newSize = Files.size(file);
                } catch (NoSuchFileException nf) {
                    // Rotation brutale: on stoppe proprement
                    break;
                }

                if (newSize < lastReadPos) {
                    // Truncation/rotation: repartir de 0
                    lastReadPos = 0L;
                }

                if (newSize > lastReadPos) {
                    streamFromOffset(session, file, lastReadPos);
                    lastReadPos = newSize;
                }
            }

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("LogFileWebSocketHandler tail error: " + e.getMessage());
        } finally {
            // cleanup du registre si cette session est toujours enregistrée
            WebSocketSession cur = liveByKey.get(key);
            if (cur == session) {
                liveByKey.remove(key);
            }
            safeClose(session, CloseStatus.NORMAL);
        }
    }

    /** Stream ligne par ligne à partir d’un offset de fichier. */
    private void streamFromOffset(WebSocketSession session, Path file, long offset) throws IOException {
        try (InputStream fis = new FileInputStream(file.toFile())) {
            if (offset > 0)
                fis.skip(offset);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
                String line;
                while (session.isOpen() && (line = reader.readLine()) != null) {
                    sendSafe(session, line + "\n");
                }
            }
        }
    }

    /** Envoi "safe" qui ignore les erreurs de transport. */
    private void sendSafe(WebSocketSession session, String payload) {
        try {
            if (session.isOpen())
                session.sendMessage(new TextMessage(payload));
        } catch (Exception ignore) {
            /* on laisse la boucle détecter la fermeture */ }
    }

    /** Fermeture "safe". */
    private void safeClose(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen())
                session.close(status);
        } catch (Exception ignore) {
        }
    }
}
