package com.undefinedbehaviourgames.callswitch;

import static database.Schema.Contact.Cols.id;

import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

public class Contact implements Serializable {

    private String mName;
    private String mPhone;
    private Long mId;
    private boolean deleted;
    private ArrayList<UUID> mReplies;
    private UUID mActiveReplyId;
    private int mColor;
    private int mSecondaryColor;

    public Contact() {
        mName = "";
        mPhone = "";
        mReplies = new ArrayList<>();
    }

    public void updateActiveReply(Context context, Reply reply, boolean replaceEqualPriority) {

        Reply activeReply = ReplyLab.getInstance(context).get(mActiveReplyId);

        //if there is no current active reply then set active reply to reply argument;
        //not enabled replies can be set as active reply if active reply is null
        if (activeReply == null) {
            mActiveReplyId = reply.getId();
            return;
        }

        //only enabled replies should replace active replies
        if (!reply.isEnabled()) return;

        //at this point new reply is enabled, if active reply is not enabled replace
        if (!activeReply.isEnabled()) {
            mActiveReplyId = reply.getId();
            return;
        }

        //if reply argument has a higher priority than active reply, then replace active reply
        //or if user sets replace equal priority to true and reply has equal priority then replace active reply
        if (reply.getPriority().ordinal() > activeReply.getPriority().ordinal()) {
            mActiveReplyId = reply.getId();
        } else if (replaceEqualPriority && reply.getPriority().ordinal() == activeReply.getPriority().ordinal()) {
            mActiveReplyId = reply.getId();
        }

    }

    public boolean removeReply(Context context, Reply reply) {

        //remove reply if in reply list
        for (int i = 0; i < mReplies.size(); i++) {
            if (reply.getId().equals(mReplies.get(i))) {
                mReplies.remove(i);
            }
        }
        //if reply is also active reply, make active reply null;
        if (mActiveReplyId != null && reply.getId().equals(mActiveReplyId)){
            mActiveReplyId = null;
            return true;
        }

        return false;
    }

    public void addReply(Reply reply) {

        //add reply if reply is not already in list
        for (UUID id : mReplies) {
            if (reply.getId().equals(id)) return;
        }
        mReplies.add(reply.getId());
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    public int getSecondaryColor() {
        return mSecondaryColor;
    }

    public void setSecondaryColor(int secondaryColor) {
        mSecondaryColor = secondaryColor;
    }

    public String getIcon() {
        String result;
        try {
            result = mName.substring(0,1);
        } catch (IndexOutOfBoundsException e) {
           result = "?";
        }

        return result;
    }

    public UUID getActiveReplyId(Context context) {
        Reply reply = ReplyLab.getInstance(context).get(mActiveReplyId);
        if (reply == null) {
            mActiveReplyId = null;
            ContactLab.getInstance(context).update(this);
        }
        return mActiveReplyId;
    }

    public UUID getActiveReplyId() {
        return mActiveReplyId;
    }

    public void setActiveReplyId(UUID id) {
        mActiveReplyId = id;
    }

    public String getActiveReplyText(Context context) {
        Reply reply = ReplyLab.getInstance(context).get(mActiveReplyId);

        if (reply == null) return "";

        return reply.getReply();
    }

    public Priority getActiveReplyPriority(Context context) {
        Reply reply = ReplyLab.getInstance(context).get(mActiveReplyId);

        if (reply == null) {
            return Priority.DEFAULT;
        }

        return reply.getPriority();
    }

    public boolean isActiveReplyEnabled(Context context) {
        Reply reply = ReplyLab.getInstance(context).get(mActiveReplyId);

        if (reply == null ) return false;

        return reply.isEnabled();
    }

    public ArrayList<UUID> getReplies() {
        return mReplies;
    }

    public ArrayList<Reply> getReplies(Context context) {
        //make asynchronous
        ArrayList<Reply> replies = new ArrayList<>();

        for (int i = 0; i< mReplies.size(); i++) {
            Reply reply = ReplyLab.getInstance(context).get(mReplies.get(i));
            if (reply != null) replies.add(reply);
            else {
                //if reply is null, then it has probably been deleted,
                //remove the reply from reply list
                mReplies.remove(i);
            }
        }

        //update contact since some replies may have been null
        ContactLab.getInstance(context).update(this);
        return replies;
    }

    public void setReplies(ArrayList<UUID> replies) {
        mReplies = replies;
    }
}
