package com.rulebased848.gamsibot.core;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Properties;
import javax.mail.Authenticator;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import static javax.mail.Transport.send;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
    private final Session session;

    private final String mailAddress;

    @Autowired
    public EmailUtil(MailProps mailProps) {
        var props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", mailProps.getHost());
        props.put("mail.smtp.port", mailProps.getPort());
        props.put("mail.smtp.ssl.trust", mailProps.getSslTrust());
        props.put("mail.smtp.ssl.protocols", mailProps.getSslProtocols());
        var auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getUsername(), mailProps.getPassword());
            }
        };
        session = Session.getInstance(props, auth);
        mailAddress = mailProps.getAddress();
    }

    public void sendEmail(
        String emailAddress,
        String channelId,
        long subscriberCount,
        Instant timestamp
    ) throws UnsupportedEncodingException, MessagingException {
        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailAddress, "Gamsi Bot"));
        message.setRecipients(TO, InternetAddress.parse(emailAddress));
        message.setSubject("Gamsi Bot Notification");
        var bodyPart = new MimeBodyPart();
        var content = "Channel ID: <a href=\"https://www.youtube.com/channel/" + channelId + "\">" + channelId + "</a><br>" +
            "Subscribers: " + subscriberCount + "<br>" +
            "UTC Timestamp: " + timestamp;
        bodyPart.setContent(content, "text/html; charset=utf-8");
        var multiPart = new MimeMultipart();
        multiPart.addBodyPart(bodyPart);
        message.setContent(multiPart);
        send(message);
    }
}