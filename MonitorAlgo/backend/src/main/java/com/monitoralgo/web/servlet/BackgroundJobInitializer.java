package com.monitoralgo.web.servlet;

import com.monitoralgo.alerting.service.LogDetectorService;
import com.monitoralgo.alerting.service.SmtpService;
import com.monitoralgo.config.PropertiesLoader;
import com.monitoralgo.core.dao.LogPatternDao;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.util.Properties;

public class BackgroundJobInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Properties props = null;
        try {
            // Essaie de charger "config/application.properties" (disque OU classpath)
            props = PropertiesLoader.load("config/application.properties");
        } catch (Exception e) {
            try {
                // Fallback sur racine du classpath si pas trouvé plus haut
                props = PropertiesLoader.load("application.properties");
            } catch (Exception ex) {
                System.err.println("Impossible de charger application.properties : " + ex.getMessage());
                props = new Properties();
            }
        }

        LogPatternDao patternDao = new LogPatternDao();

        String logFolder = props.getProperty("log.watcher.folder", "logs");

        SmtpService smtpService = null;
        if (props.getProperty("smtp.host") != null &&
                props.getProperty("smtp.port") != null &&
                props.getProperty("smtp.from") != null &&
                props.getProperty("smtp.to") != null) {
            smtpService = SmtpService.fromProperties(props);
        }

        LogDetectorService logDetector = new LogDetectorService(patternDao, smtpService, logFolder);
        logDetector.init();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Rien à faire (arrêt des threads si besoin)
    }
}
