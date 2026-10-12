// monitoralgo/algo/service/AlgoStatsService.java
package com.monitoralgo.algo.service;

import com.monitoralgo.api.model.AlgoUpdateStatsResponse;
import com.monitoralgo.core.dao.PatternStrategyStatsDao;
import com.monitoralgo.core.util.logger.LoggerFactory;
import com.monitoralgo.core.util.logger.ProcessLogger;

import org.springframework.stereotype.Service;

@Service
public class AlgoUpdateStatsService {

    private final String STUDY_ID = "ALGO";
    private final String INSTANCE_NAME = "UPDATE-STATS";
    private final PatternStrategyStatsDao statsDao = new PatternStrategyStatsDao();

    public AlgoUpdateStatsResponse updatePatternStrategyStats() {

        // API handler
        ProcessLogger LOGGER = LoggerFactory.openProcessLogger(STUDY_ID, INSTANCE_NAME);
        String runId = LOGGER.getRunId(); // renvoyer ça au front

        new Thread(() -> {
            try (LOGGER) {
                final String instanceId = String.valueOf(System.identityHashCode(this));
                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[INIT]");
                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "Création Fichier de Log Algo Update Stats");

                statsDao.updatePatternStrategyStatsFull(LOGGER, STUDY_ID, INSTANCE_NAME);

                LOGGER.setContext(instanceId, STUDY_ID, INSTANCE_NAME, "[CLOSE]");
            } catch (Exception e) {
                System.err.println("AlgoUpdateStatsService updatePatternStrategyStats error: " + e.getMessage());
            } // close() automatique -> fichier scellé pour CETTE exécution
        }).start();

        // Retour immédiat de la validation avec runId
        return new AlgoUpdateStatsResponse(
                true,
                "Update Algo Stats lancé en tâche de fond : " + runId);
    }
}
