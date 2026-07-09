package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class SelectContactLab extends ContactLabHelper<SelectContact> {


    private List<Contact> mPreviousSelectedContacts;
    private List<SelectContact> mContacts;
    public SelectContactLab(Context context) {
        super(context, SelectContact.class);
        mPreviousSelectedContacts = new ArrayList<>();
        mContacts = new ArrayList<>();
    }


    public ArrayList<Long> getSelectedContacts() {
        ArrayList<Long> selectedContacts = new ArrayList<>();
        for (SelectContact contact : mContacts) {
            if (contact.isChecked()) {
                selectedContacts.add(contact.getId());
            }
        }
        return selectedContacts;
    }

    @Override
    public List<SelectContact> getContacts() {
        mContacts = super.getContacts();

        for (SelectContact contact : mContacts) {
            contact.setChecked(isPreviousSelected(contact.getId()));
        }

        return mContacts;
    }

    public List<SelectContact> getContacts(boolean _new) {

        if (_new) return getContacts();
        return mContacts;
    }

    public List<SelectContact> getContacts(String queryString) {
        List<SelectContact> contacts = super.getContacts(queryString);

        for (SelectContact contact : contacts) {
            contact.setChecked(isSelected(contact.getId()));
        }

        return contacts;
    }

    public void setPreviousSelectedContacts(List<Contact> selectedContacts) {
        mPreviousSelectedContacts = selectedContacts;
    }

    public boolean isPreviousSelected(Long id) {
        //make asynchronous
        for (Contact contact : mPreviousSelectedContacts) {
            if (contact.getId().equals(id)) {
                return true;
            }
        }

        return false;
    }

    public boolean isSelected(Long id) {
        //make asynchronous
        for (SelectContact contact : mContacts) {
            if (contact.getId().equals(id)) {
                return contact.isChecked();
            }
        }

        return false;
    }

    public void setSelectAllContacts(boolean isChecked) {

        for (SelectContact contact : mContacts) {
            contact.setChecked(isChecked);
        }

    }







}
