package com.rulebased848.gamsibot.core;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
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

    private final EmailView view;

    private final String mailAddress;

    @Autowired
    public EmailUtil(Session session, EmailView view, MailProps mailProps) {
        this.session = session;
        this.view = view;
        mailAddress = mailProps.getAddress();
    }

    public void sendEmail(
        String emailAddress,
        String handle,
        long subscriberCount,
        Instant timestamp
    ) throws UnsupportedEncodingException, MessagingException {
        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(mailAddress, view.getPersonalName()));
        message.setRecipients(TO, InternetAddress.parse(emailAddress));
        message.setSubject(view.getSubject());
        var bodyPart = new MimeBodyPart();
        var content = view.getContent(handle, subscriberCount, timestamp);
        bodyPart.setContent(content, "text/html; charset=utf-8");
        var multiPart = new MimeMultipart();
        multiPart.addBodyPart(bodyPart);
        message.setContent(multiPart);
        send(message);
    }
}