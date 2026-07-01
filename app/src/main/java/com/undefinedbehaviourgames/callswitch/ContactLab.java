package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class ContactLab extends ContactLabHelper<Contact> {

    private static ContactLab sContactLab;
//    private List<Contact> mContacts;

    private ContactLab(Context context) {
        super(context);
//        mContacts = new ArrayList<>();
    }

    public static ContactLab getInstance(Context context) {

        if (sContactLab == null) {
            sContactLab = new ContactLab(context);
        }

        return sContactLab;
    }

//    public void add(Contact contact) {
//        mContacts.add(contact);
//    }

//    public List<Contact> getContacts() {
//        return mContacts;
//    }

}
