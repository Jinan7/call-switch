package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
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
    private boolean enabled;
    private List<Contact> mReplyToList;
    public Reply() {
        mUuid = UUID.randomUUID();
        mPriority = Priority.NORMAL;
        mReplyToList = new ArrayList<>();
        mReply = "";
    }

    public UUID getId() {
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

    public List<Contact> getReplyToList() {
        return mReplyToList;
    }

    public void setReplyToList(List<Contact> replyToList) {
        mReplyToList = replyToList;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPriorityText(Context context) {
        String result = "";
        switch (mPriority) {
            case HIGH:
                result = context.getResources().getString(R.string.priority_high);
                break;
            case NORMAL:
                result = context.getResources().getString(R.string.priority_normal);
                break;
            case LOW:
                result = context.getResources().getString(R.string.priority_low);
                break;

        }

        return result;
    }
}
