package com.rulebased848.gamsibot.core;

import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class EmailView {
    public String getPersonalName() {
        return "Gamsi Bot";
    }

    public String getSubject() {
        return "Gamsi Bot Notification";
    }

    public String getContent(String handle, long subscriberCount, Instant timestamp) {
        return "Handle: <a href=\"https://www.youtube.com/@" + handle + "\">@" + handle + "</a><br>" +
            "Subscribers: " + Long.toUnsignedString(subscriberCount) + "<br>" +
            "UTC Timestamp: " + timestamp;
    }

    public String getImageFileName() {
        return "screenshot.png";
    }
}