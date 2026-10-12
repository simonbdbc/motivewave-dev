package com.monitoralgo.monitoring.controller;

import org.springframework.web.bind.annotation.*;

import com.monitoralgo.config.GlobalConfig;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Contrôleur exposant un arbre de fichiers/logs pour plusieurs sources.
 * - /api/tree?source=strategy
 * - /api/tree?source=motivewave
 * - /api/tree?source=algo-backtest
 * - /api/tree?source=algo-update-stats
 *
 * Règles de tri :
 * - Dossiers : triés par nom (desc)
 * - Fichiers : triés par date de dernière modification (desc)
 */
@RestController
@RequestMapping("/api")
public class LogTreeController {

    // Racines lues depuis la configuration globale
    private String strategyRoot = GlobalConfig.get("STRATEGY_LOG_FOLDER");
    private String motivewaveRoot = GlobalConfig.get("MOTIVEWAVE_LOG_FOLDER");
    private String algoRoot = GlobalConfig.get("ALGO_LOG_FOLDER");

    @GetMapping("/tree")
    public List<FileNode> getLogTree(@RequestParam String source) {
        try {
            Path root;
            boolean acceptStrategyLog = false;
            boolean acceptMotivewaveTxt = false;
            boolean acceptAlgoLog = false;
            boolean acceptAlgoUpdateStatsLog = false;

            // Sélection de la source + flags d'acceptation
            if ("strategy".equals(source)) {
                root = Paths.get(strategyRoot);
                acceptStrategyLog = true;
            } else if ("motivewave".equals(source)) {
                root = Paths.get(motivewaveRoot);
                acceptMotivewaveTxt = true;
            } else if ("algo-backtest".equals(source)) {
                root = Paths.get(algoRoot);
                acceptAlgoLog = true;
            } else if ("algo-update-stats".equals(source)) {
                root = Paths.get(algoRoot);
                acceptAlgoUpdateStatsLog = true;
            } else {
                throw new IllegalArgumentException("Source inconnue: " + source);
            }

            System.out.println("LogTreeController: source=" + source + ", root=" + root);
            if (!Files.exists(root)) {
                System.out.println("Le dossier racine n'existe pas : " + root);
            }

            List<FileNode> res = listDirectory(
                    root, root, acceptStrategyLog, acceptMotivewaveTxt, acceptAlgoLog, acceptAlgoUpdateStatsLog);
            return res != null ? res : Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    /**
     * Parcours récursif du dossier 'dir' à partir de 'root'.
     * - Ne conserve que les fichiers selon les flags
     * - N'ajoute un dossier (yyyy-mm-dd) que s'il a des enfants retenus
     *
     * Tri final :
     * - Dossiers : nom desc
     * - Fichiers : lastModified desc
     */
    private List<FileNode> listDirectory(Path root, Path dir,
            boolean acceptStrategyLog,
            boolean acceptMotivewaveTxt,
            boolean acceptAlgoLog,
            boolean acceptAlgoUpdateStatsLog) throws IOException {
        List<FileNode> dirs = new ArrayList<>();
        List<FileNode> files = new ArrayList<>();

        if (!Files.exists(dir))
            return files;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();

                if (Files.isDirectory(path)) {
                    // On ne descend que dans les dossiers au format YYYY-MM-DD
                    if (name.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        List<FileNode> children = listDirectory(
                                root, path, acceptStrategyLog, acceptMotivewaveTxt, acceptAlgoLog,
                                acceptAlgoUpdateStatsLog);

                        // On garde le dossier uniquement s'il contient des enfants retenus
                        if (children != null && !children.isEmpty()) {
                            FileNode node = new FileNode();
                            node.name = name;
                            node.path = root.relativize(path).toString();
                            node.isDirectory = true;
                            node.children = children;
                            // lastModified non nécessaire pour le tri des dossiers
                            dirs.add(node);
                        }
                    }
                } else if (Files.isRegularFile(path)
                        && ((acceptStrategyLog && name.endsWith(".log"))
                                || (acceptMotivewaveTxt && name.startsWith("output") && name.endsWith(".txt"))
                                || (acceptAlgoLog && name.startsWith("ALGO_BACKTEST") && name.endsWith(".log"))
                                || (acceptAlgoUpdateStatsLog && name.startsWith("ALGO_UPDATE-STATS")
                                        && name.endsWith(".log")))) {

                    // Création d'un FileNode pour le fichier + récupération de la date de MAJ
                    FileNode node = new FileNode();
                    node.name = name;
                    node.path = root.relativize(path).toString();
                    node.isDirectory = false;

                    // IMPORTANT : on stocke la date de dernière modif pour trier ensuite
                    node.lastModified = Files.getLastModifiedTime(path).toMillis();

                    files.add(node);
                }
            }
        }

        // Tri des dossiers : par nom décroissant
        dirs.sort(Comparator.comparing((FileNode n) -> n.name).reversed());

        // Tri des fichiers : par dernière modification décroissante (plus récent en
        // premier)
        files.sort(Comparator.comparingLong((FileNode n) -> n.lastModified).reversed());

        // Concaténation dossiers puis fichiers
        List<FileNode> nodes = new ArrayList<>(dirs.size() + files.size());
        nodes.addAll(dirs);
        nodes.addAll(files);
        return nodes;
    }

    /**
     * Représente un noeud (dossier ou fichier) de l'arbre.
     * - lastModified est utilisé uniquement pour trier les fichiers (non nécessaire
     * pour les dossiers).
     */
    public static class FileNode {
        public String name;
        public String path;
        public boolean isDirectory;
        public List<FileNode> children;

        // Nouveau champ pour supporter le tri par date de MAJ (en millisecondes epoch).
        public long lastModified;
    }
}
