package com.undefinedbehaviourgames.callswitch;

import java.io.Serializable;

public class Contact implements Serializable {

    private String mName;
    private String mPhone;
    private Long mId;

    public Contact() {
        mName = "";
        mPhone = "";
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
}
