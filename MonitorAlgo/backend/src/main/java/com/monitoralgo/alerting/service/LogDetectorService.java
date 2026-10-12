package com.monitoralgo.alerting.service;

import java.time.Duration;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.monitoralgo.core.dao.LogPatternDao;
import com.monitoralgo.core.model.LogPattern;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

public class LogDetectorService {

    private static final Logger log = LoggerFactory.getLogger(LogDetectorService.class);

    private final LogPatternDao patternDao;
    private final SmtpService smtpService; // Nullable
    private final String logFolder;

    public LogDetectorService(LogPatternDao patternDao, SmtpService smtpService, String logFolder) {
        this.patternDao = patternDao;
        this.smtpService = smtpService;
        this.logFolder = logFolder != null ? logFolder : "logs";
    }

    // Appelle manuellement cette méthode au démarrage de l'app (ou via main)
    public void init() {
        File folder = new File(logFolder);
        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".log"));
            if (files != null) {
                for (File file : files) {
                    startTailer(file);
                }
            }
        }
    }

    public void startTailer(File logFile) {
        Tailer tailer = Tailer.builder()
                .setFile(logFile)
                .setTailerListener(new TailerListenerAdapter() {
                    @Override
                    public void handle(String line) {
                        try {
                            List<LogPattern> patterns = patternDao.findAll();
                            for (LogPattern pat : patterns) {
                                if (pat.isActive() && Pattern.compile(pat.getPattern()).matcher(line).find()) {
                                    log.info("Motif '{}' détecté dans {} ! Envoi mail.", pat.getPattern(),
                                            logFile.getName());
                                    if (smtpService != null) {
                                        smtpService.sendMail(pat.getMessage() + "\n\nLigne détectée :\n" + line);
                                    } else {
                                        log.warn("Service SMTP inactif, email non envoyé");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("Erreur détection log : {}", e.getMessage());
                        }
                    }
                })
                .setDelayDuration(Duration.ofMillis(1000))
                .setTailFromEnd(true)
                .get();

        Thread tailerThread = new Thread(tailer);
        tailerThread.setDaemon(true);
        tailerThread.start();
    }

}
