package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import database.ContactCursorWrapper;
import database.Schema;

public class SelectContactLab extends ContactLabHelper<SelectContact> {


    private List<Contact> mPreviousSelectedContacts;
    private List<SelectContact> mContacts;

    private HashSet<String> mSelectContactsSet;
    public SelectContactLab(Context context) {
        super(context, SelectContact.class);
//        mPreviousSelectedContacts = new ArrayList<>();
        mContacts = new ArrayList<>();
        mSelectContactsSet = new HashSet<>();
    }


    public ArrayList<String> getSelectedContacts() {

        synchronized (this) {
            ArrayList<String> selectedContacts = new ArrayList<>();
            for (SelectContact contact : mContacts) {
                if (contact.isChecked()) {
                    selectedContacts.add(contact.getLookupKey());
                }
            }
            return selectedContacts;
        }

    }

    public void setSelectedContactsSet(HashSet<String> selectContactsSet) {
        mSelectContactsSet = selectContactsSet;
    }

    public HashSet<String> getSelectedContactsSet() {
        synchronized (this) {
            return mSelectContactsSet;
        }
    }

    @Override
    public List<SelectContact> getContacts(WeakReference<Callbacks<SelectContact>> callbacksWeakReference) {
        synchronized (this) {
            List<SelectContact> contacts = new ArrayList<>();
            ContactCursorWrapper<SelectContact> cursor = queryDatabase(null, null, Schema.Contact.Cols.name + " ASC");
            contacts = getContacts(cursor, callbacksWeakReference);
            return contacts;
        }

    }

    @Override
    public List<SelectContact> getContacts(ContactCursorWrapper<SelectContact> cursor, WeakReference<Callbacks<SelectContact>> callbacksWeakReference) {

        synchronized (this) {
            List<SelectContact> contacts = new ArrayList<>();

            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    SelectContact contact = (SelectContact) cursor.getContact();
                    //only check if contact has been deleted once contact query handler fetches all contacts
                    //if not any contact that has not yet been fetched will be marked as deleted temporarily since
                    //it will not be in the list of contacts
                    if (ContactQueryHandler.getInstance(mContext).getQueryState() == State.FETCHED) contact.setDeleted(isDeleted(contact));
                    contact.setChecked(isSelected(contact.getLookupKey()));
                    contacts.add(contact);

                    if (callbacksWeakReference.get() != null) {
                        callbacksWeakReference.get().onGetSingleContact(contact);
                    }
                    cursor.moveToNext();
                }

                final List<SelectContact> immutableContactSnapshot = new ArrayList<>(contacts);

                if (callbacksWeakReference != null) {
                    callbacksWeakReference.get().onGetAllContacts(immutableContactSnapshot);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                cursor.close();
            }
            return contacts;
        }

    }

    public List<SelectContact> getContacts(boolean _new) {

        synchronized (this) {
            if (_new) return getContacts();
            return mContacts;
        }

    }

    @Override
    public List<SelectContact> getContacts(String queryString) {

        synchronized (this) {
            List<SelectContact> contacts = super.getContacts(queryString);

            for (SelectContact contact : contacts) {
                contact.setChecked(isSelected(contact.getLookupKey()));
            }

            return contacts;
        }

    }

    @Override
    public List<SelectContact> getContacts(String queryString, WeakReference<SearchCallbacks<SelectContact>> callbacksWeakReference) {

        synchronized (this) {
            List<SelectContact> contacts = super.getContacts(queryString, callbacksWeakReference);

            for (SelectContact contact : contacts) {
                contact.setChecked(isSelected(contact.getLookupKey()));
            }

            return contacts;
        }

    }

    public void setPreviousSelectedContacts(List<Contact> selectedContacts) {
        synchronized (this) {
            for (Contact contact : selectedContacts) {
                mSelectContactsSet.add(contact.getLookupKey());
            }
//            mPreviousSelectedContacts = selectedContacts;
        }

    }

//    public boolean isPreviousSelected(String lookupkey) {
//        //make asynchronous
//        synchronized (this) {
//            for (Contact contact : mPreviousSelectedContacts) {
//                if (contact.getLookupKey().equals(lookupkey)) {
//                    return true;
//                }
//            }
//
//            return false;
//        }
//
//    }

    public boolean isSelected(String lookupkey) {
        //make asynchronous
        synchronized (this) {
            if (mSelectContactsSet.contains(lookupkey)) return true;
            return false;
        }

        //            for (SelectContact contact : mContacts) {
        //                if (contact.getLookupKey().equals(lookupkey)) {
        //                    return contact.isChecked();
        //                }
        //            }

    }

    public void setSelectAllContacts(boolean isChecked, WeakReference<SelectContactCallbacks> callbacksWeakReference) {

        synchronized (this) {
            for (SelectContact contact : mContacts) {
                contact.setChecked(isChecked);
                setSelectContact(contact.getLookupKey(), isChecked);
            }

            if (callbacksWeakReference.get() != null) {
                callbacksWeakReference.get().onSetSelectAllContacts();
            }

        }
    }

    public void setSelectContact(String lookupKey, boolean isChecked) {
        if (isChecked) {
            mSelectContactsSet.add(lookupKey);
        } else {
            mSelectContactsSet.remove(lookupKey);
        }
    }

    public  void setContacts(List<SelectContact> contacts) {
        synchronized (this) {
            mContacts = contacts;
        }

    }


    public interface SelectContactCallbacks extends Callbacks<SelectContact> {

        void onSetSelectAllContacts();
    }

}
