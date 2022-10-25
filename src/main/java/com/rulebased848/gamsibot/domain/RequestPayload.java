package com.rulebased848.gamsibot.domain;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

public class RequestPayload {
    @NotBlank
    private String channelId;

    private long targetSubscriberCount;

    @Email
    @NotEmpty
    private String emailAddress;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public long getTargetSubscriberCount() {
        return targetSubscriberCount;
    }

    public void setTargetSubscriberCount(long targetSubscriberCount) {
        this.targetSubscriberCount = targetSubscriberCount;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}