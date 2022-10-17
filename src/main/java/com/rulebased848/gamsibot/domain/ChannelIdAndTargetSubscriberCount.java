package com.rulebased848.gamsibot.domain;

public class ChannelIdAndTargetSubscriberCount {
    private String channelId;

    private long targetSubscriberCount;

    public ChannelIdAndTargetSubscriberCount(String channelId, long targetSubscriberCount) {
        this.channelId = channelId;
        this.targetSubscriberCount = targetSubscriberCount;
    }

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
}