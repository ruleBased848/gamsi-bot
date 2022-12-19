package com.rulebased848.gamsibot.core;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import javax.mail.BodyPart;
import javax.mail.Message;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import static javax.mail.Transport.send;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
    private static final Logger logger = getLogger(EmailUtil.class);

    private final Session session;

    private final EmailView view;

    private final String mailAddress;

    private final ScreenshotTaker screenshotTaker;

    @Autowired
    public EmailUtil(Session session, EmailView view, MailProps mailProps, ScreenshotTaker screenshotTaker) {
        this.session = session;
        this.view = view;
        mailAddress = mailProps.getAddress();
        this.screenshotTaker = screenshotTaker;
    }

    public boolean sendEmail(String emailAddress, String handle, long subscriberCount, Instant timestamp) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailAddress, view.getPersonalName()));
            message.setRecipients(TO, InternetAddress.parse(emailAddress));
            message.setSubject(view.getSubject());
            Multipart multipart = new MimeMultipart();
            BodyPart bodyPart = new MimeBodyPart();
            Object content = view.getContent(handle, subscriberCount, timestamp);
            bodyPart.setContent(content, "text/html; charset=utf-8");
            multipart.addBodyPart(bodyPart);
            BodyPart screenshot = new MimeBodyPart();
            screenshot.setDataHandler(screenshotTaker.getScreenshot("https://www.youtube.com/@" + handle));
            screenshot.setFileName(view.getImageFileName());
            multipart.addBodyPart(screenshot);
            message.setContent(multipart);
            send(message);
            return true;
        } catch (UnsupportedEncodingException uee) {
            logger.error("Check mail.address property.", uee);
        } catch (MessagingException me) {
            logger.error("", me);
        }
        return false;
    }
}