package com.rulebased848.gamsibot.domain;

public class HandleAndTargetSubscriberCount {
    private String handle;

    private long targetSubscriberCount;

    public HandleAndTargetSubscriberCount(String handle, long targetSubscriberCount) {
        this.handle = handle;
        this.targetSubscriberCount = targetSubscriberCount;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public long getTargetSubscriberCount() {
        return targetSubscriberCount;
    }

    public void setTargetSubscriberCount(long targetSubscriberCount) {
        this.targetSubscriberCount = targetSubscriberCount;
    }
}