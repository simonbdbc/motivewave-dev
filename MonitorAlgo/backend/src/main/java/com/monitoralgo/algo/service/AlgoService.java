package com.monitoralgo.algo.service;

import com.monitoralgo.api.model.AlgoRunRequest;
import com.monitoralgo.api.model.AlgoRunResponse;
// import com.monitoralgo.core.util.CoreLogger;
import com.monitoralgo.core.util.logger.LoggerFactory;
import com.monitoralgo.core.util.logger.ProcessLogger;

// import java.io.PrintWriter;
// import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class AlgoService {

    private final String STUDY_ID = "ALGO";
    private final String INSTANCE_NAME = "BACKTEST";
    private final AlgoBacktestCore algoBacktestCore = new AlgoBacktestCore();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AlgoRunResponse runAlgo(AlgoRunRequest request) {

        // Lancement du traitement en tâche de fond avec un Thread anonyme
        // new Thread(() -> {
        // try {
        // // Identifiants pour ce process
        // final String STUDY_ID = "ALGO_BACKTEST";
        // final String instanceId = String.valueOf(System.identityHashCode(this));
        // // Singleton du logger pour tout le process
        // CoreLogger LOGGER = CoreLogger.getInstance(STUDY_ID, "main");
        // // Création immédiate du fichier de log
        // LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "Création Fichier de
        // Log Algo");

        // LOGGER.log("--------------------------");
        // LOGGER.log("Début du Thread Run Backtest");
        // LOGGER.log("--------------------------");

        // if (request.getDateFrom() != null && request.getDateTo() != null) {
        // LocalDateTime dateFrom = request.getDateFrom().atStartOfDay();
        // LocalDateTime dateTo = request.getDateTo().atTime(LocalTime.MAX);
        // LOGGER.log("Lancement de l'algo backtest du " + dateFrom.format(DATE_FMT) + "
        // au "
        // + dateTo.format(DATE_FMT));
        // algoBacktestCore.runBacktest(dateFrom, dateTo, false);
        // } else {

        // LOGGER.log("Lancement de l'algo backtest sur la totalité des Snapshots");
        // algoBacktestCore.runBacktest(null, null, true);
        // }

        // LOGGER.log("--------------------------");
        // LOGGER.log("Fin du Thread Run Backtest");
        // LOGGER.log("--------------------------");
        // LOGGER.close();
        // } catch (Exception e) {
        // System.err.println("AlgoService runAlgo error: " + e.getMessage());
        // }
        // }).start();

        // API handler
        ProcessLogger LOGGER = LoggerFactory.openProcessLogger(STUDY_ID, INSTANCE_NAME);
        String runId = LOGGER.getRunId(); // renvoyer ça au front

        new Thread(() -> {
            try (LOGGER) {
                final String instanceId = String.valueOf(System.identityHashCode(this));
                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[INIT]");
                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "  ✔ Création Fichier de Log Algo Update Stats");

                // statsDao.updatePatternStrategyStatsFull(LOGGER, STUDY_ID, INSTANCE_NAME);

                if (request.getDateFrom() != null && request.getDateTo() != null) {
                    LocalDateTime dateFrom = request.getDateFrom().atStartOfDay();
                    LocalDateTime dateTo = request.getDateTo().atTime(LocalTime.MAX);
                    LOGGER.log("  ✔ Lancement de l'algo backtest du " + dateFrom.format(DATE_FMT) + " au "
                            + dateTo.format(DATE_FMT));
                    algoBacktestCore.runBacktest(LOGGER, STUDY_ID, INSTANCE_NAME, dateFrom, dateTo, false);
                } else {

                    LOGGER.log("  ✔ Lancement de l'algo backtest sur la totalité des Snapshots");
                    algoBacktestCore.runBacktest(LOGGER, STUDY_ID, INSTANCE_NAME, null, null, true);
                }

                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[CLOSE]");
            } catch (Exception e) {
                System.err.println("AlgoService runAlgo error: " + e.getMessage());
            } // close() automatique -> fichier scellé pour CETTE exécution
        }).start();

        // Retour immédiat de la validation
        return new AlgoRunResponse(true, "Run Algo lancé en tâche de fond : " + runId);
    }
}
