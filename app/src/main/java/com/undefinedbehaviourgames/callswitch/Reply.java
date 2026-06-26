package com.undefinedbehaviourgames.callswitch;

import java.util.UUID;

enum Priority {
    LOW,
    NORMAL,
    HIGH
}
public class Reply {

    private UUID mUuid;
    private Priority mPriority;
    private String mReply;

    public Reply() {
        mUuid = UUID.randomUUID();
        mPriority = Priority.NORMAL;
        mReply = "";
    }

    public UUID getUuid() {
        return mUuid;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    public String getReply() {
        return mReply;
    }

    public void setReply(String reply) {
        mReply = reply;
    }
}
