package com.undefinedbehaviourgames.callswitch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    public ArrayList<UUID> getReplies() {
        return mReplies;
    }

    public void setReplies(ArrayList<UUID> replies) {
        mReplies = replies;
    }
}
