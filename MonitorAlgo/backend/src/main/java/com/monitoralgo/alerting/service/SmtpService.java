package com.monitoralgo.alerting.service;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class SmtpService {

    private static final Logger log = LoggerFactory.getLogger(SmtpService.class);

    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUser;
    private final String smtpPass;
    private final String from;
    private final String to;
    private final boolean ssl;
    private Mailer mailer;

    // Constructeur explicite
    public SmtpService(String smtpHost, int smtpPort, String smtpUser, String smtpPass,
            String from, String to, boolean ssl) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUser = smtpUser;
        this.smtpPass = smtpPass;
        this.from = from;
        this.to = to;
        this.ssl = ssl;
        init();
    }

    private void init() {
        this.mailer = MailerBuilder.withSMTPServer(smtpHost, smtpPort, smtpUser, smtpPass)
                .withTransportStrategy(ssl ? TransportStrategy.SMTPS : TransportStrategy.SMTP)
                .buildMailer();
        log.info("SMTP Mailer initialisé pour host={}, user={}", smtpHost, smtpUser);
    }

    public void sendMail(String content) {
        try {
            Email email = EmailBuilder.startingBlank()
                    .from(from)
                    .to(to)
                    .withSubject("Alerte MotiveWave Log")
                    .withPlainText(content)
                    .buildEmail();
            mailer.sendMail(email);
            log.info("Email envoyé avec succès.");
        } catch (Exception e) {
            log.error("Erreur envoi email: {}", e.getMessage());
        }
    }

    public static SmtpService fromProperties(Properties props) {
        return new SmtpService(
                props.getProperty("smtp.host"),
                Integer.parseInt(props.getProperty("smtp.port", "25")),
                props.getProperty("smtp.username"),
                props.getProperty("smtp.password"),
                props.getProperty("smtp.from"),
                props.getProperty("smtp.to"),
                Boolean.parseBoolean(props.getProperty("smtp.ssl", "false")));
    }
}
