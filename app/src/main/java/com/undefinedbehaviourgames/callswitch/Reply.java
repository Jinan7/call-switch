package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

enum Priority {
    LOW,
    NORMAL,
    HIGH,
    DEFAULT
}
public class Reply {

    private UUID mUuid;
    private Priority mPriority;
    private String mReply;
    private boolean mEnabled;
    private boolean mReplyUnknown;
    private boolean mReplaceEqualPriority;
    private List<Long> mReplyToList;
    public Reply() {
        mUuid = UUID.randomUUID();
        mPriority = Priority.NORMAL;
        mReplyToList = new ArrayList<>();
        mReply = "";
        mEnabled = false;
    }

    public UUID getId() {
        return mUuid;
    }

    public void setId(UUID uuid) {
        mUuid = uuid;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        mPriority = Priority.values() [priority];
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

    public List<Long> getReplyToList() {
        return mReplyToList;
    }

    public List<Contact> getReplyToList(Context context) {
        //make asynchronous
        ArrayList<Contact> replyToList = new ArrayList<>();

        for (int i = 0; i<mReplyToList.size(); i++) {
            Contact contact = ContactLab.getInstance(context).get(mReplyToList.get(i));
            //if contact is null, then it has probably been deleted,
            //remove the contact from reply to list
            if (contact != null) replyToList.add(contact);

        }

        return replyToList;
    }

    public void setReplyToList(List<Long> replyToList) {
        mReplyToList = replyToList;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean replyUnknown() {
        return mReplyUnknown;
    }

    public void setReplyUnknown(boolean replyUnknown) {
        mReplyUnknown = replyUnknown;
    }

    public boolean replaceEqualPriority() {
        return mReplaceEqualPriority;
    }

    public void setReplaceEqualPriority(boolean replaceEqualPriority) {
        mReplaceEqualPriority = replaceEqualPriority;
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

    public void deleteContact(Contact contact) {
        //make asynchronous

        for (int i = 0; i < mReplyToList.size(); i++) {

            if (contact.getId().equals(mReplyToList.get(i))) {
                mReplyToList.remove(i);
                break;
            }
        }
    }
}
