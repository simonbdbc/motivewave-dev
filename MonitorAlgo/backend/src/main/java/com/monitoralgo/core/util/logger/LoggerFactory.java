package com.monitoralgo.core.util.logger;

import java.util.UUID;

/**
 * Factory centralisant la création des différents loggers.
 * - RotatingLogger : fichier partagé par studyId, rotation 00h/12h, ref-counting
 * - ProcessLogger  : 1 fichier par process (processName + processId), pas de rotation
 * - StudyLogger    : fichier par (studyId, instanceId), rotation horaire
 */
public final class LoggerFactory {

    private LoggerFactory() {}

    /** Logger "study" avec rotation 00h/12h et fichier partagé par studyId */
    public static RotatingLogger getRotatingLogger(String studyId, String instanceId) {
        return RotatingLogger.get(studyId, instanceId);
    }

    /** Logger "process" (1 fichier / process). Génère un UUID si processId nul. */
    public static ProcessLogger openProcessLogger(String processName, String processId) {
        String pid = (processId == null || processId.isBlank()) ? UUID.randomUUID().toString() : processId;
        return ProcessLogger.open(processName, pid);
    }

    /** Logger "study" par instance, rotation HORAIRE (path MotiveWaveLogs) */
    public static StudyLogger getStudyLogger(String studyId, String instanceId) {
        return StudyLogger.get(studyId, instanceId);
    }
}
