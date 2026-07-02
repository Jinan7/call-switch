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
    private ArrayList<UUID> mReplies;
    private UUID mActiveReplyId;

    public Contact() {
        mName = "";
        mPhone = "";
        mReplies = new ArrayList<>();
    }

    public void updateActiveReply(Context context, Reply reply) {
        Reply activeReply = ReplyLab.getInstance(context).get(mActiveReplyId);

        //if there is no current active reply then set active reply to reply argument;
        if (activeReply == null) {
            mActiveReplyId = reply.getId();
            return;
        }

        //if reply argument has a higher or equal priority than active reply, then replace active reply
        if (reply.getPriority().ordinal() >= activeReply.getPriority().ordinal()) {
            mActiveReplyId = reply.getId();
        }

    }

    public void addReply(Reply reply) {
        mReplies.add(reply.getId());
    }

    public Long getId() {
        return mId;
    }

    public void setId(Long id) {
        mId = id;
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

    public String getIcon() {
        String result;
        try {
            result = mName.substring(0,1);
        } catch (IndexOutOfBoundsException e) {
           result = "?";
        }

        return result;
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

        if (reply == null) return Priority.DEFAULT;

        return reply.getPriority();
    }

    public ArrayList<UUID> getReplies() {
        return mReplies;
    }

    public ArrayList<Reply> getReplies(Context context) {
        //make asynchronous
        ArrayList<Reply> replies = new ArrayList<>();

        for (UUID replyUuid : mReplies) {
            Reply reply = ReplyLab.getInstance(context).get(replyUuid);
            if (reply != null) replies.add(reply);
        }
        return replies;
    }

    public void setReplies(ArrayList<UUID> replies) {
        mReplies = replies;
    }
}
