package com.rulebased848.gamsibot.core;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    @Bean
    public Session session(MailProps mailProps) {
        var props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailProps.getHost());
        props.put("mail.smtp.port", mailProps.getPort());
        props.put("mail.smtp.ssl.trust", mailProps.getSslTrust());
        props.put("mail.smtp.ssl.protocols", mailProps.getSslProtocols());
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getUsername(), mailProps.getPassword());
            }
        };
        return Session.getInstance(props, auth);
    }
}