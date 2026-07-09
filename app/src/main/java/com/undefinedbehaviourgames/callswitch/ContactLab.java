package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

public class ContactLab extends ContactLabHelper<Contact> {

    private static ContactLab sContactLab;

    private ContactLab(Context context) {
        super(context, Contact.class);
    }

    public static ContactLab getInstance(Context context) {

        if (sContactLab == null) {
            sContactLab = new ContactLab(context);
        }

        return sContactLab;
    }







}
