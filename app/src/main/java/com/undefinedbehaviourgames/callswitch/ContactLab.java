package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ContactLab extends ContactLabHelper<Contact, ContactLabHelper.QueryHandler> {

    private static ContactLab sContactLab;

    private ContactLab(Context context) {
        super(context, Contact.class, QueryHandler.class);
    }

    public static ContactLab getInstance(Context context) {

        if (sContactLab == null) {
            sContactLab = new ContactLab(context);
        }

        return sContactLab;
    }

    public static ContactLab getInstance(Context context, Callbacks callbacks) {

        if (sContactLab == null) {
            sContactLab = new ContactLab(context);
            sContactLab.startQuery(callbacks);
        }

        return sContactLab;
    }






}
