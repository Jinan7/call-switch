package com.undefinedbehaviourgames.callswitch;

import java.util.ArrayList;
import java.util.List;

public class ContactLab {

    private static ContactLab sContactLab;
    private List<Contact> mContacts;

    private ContactLab() {
        mContacts = new ArrayList<>();
    }

    public static ContactLab getInstance() {

        if (sContactLab == null) {
            sContactLab = new ContactLab();
        }

        return sContactLab;
    }

    public void add(Contact contact) {
        mContacts.add(contact);
    }

    public List<Contact> getContacts() {
        return mContacts;
    }
}
