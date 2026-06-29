package com.undefinedbehaviourgames.callswitch;

public class Contact {

    private String mName;
    private String mPhone;

    public Contact() {
        mName = "";
        mPhone = "";
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
