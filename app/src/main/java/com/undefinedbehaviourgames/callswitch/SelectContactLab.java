package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.app.appsearch.SearchResults;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SelectContactLab extends ContactLabHelper<SelectContact, SelectContactLab.SelectContactQueryHandler> {


    private List<Contact> mPreviousSelectedContacts;
    private List<SelectContact> mContacts;
    public SelectContactLab(Context context, Callbacks callbacks) {
        super(context, SelectContact.class, SelectContactQueryHandler.class);
        mPreviousSelectedContacts = new ArrayList<>();
        mContacts = new ArrayList<>();
    }


    public ArrayList<Contact> getSelectedContacts() {
        ArrayList<Contact> selectedContacts = new ArrayList<>();
        for (SelectContact contact : mContacts) {
            if (contact.isChecked()) {
                selectedContacts.add((Contact) contact);
            }
        }
        return selectedContacts;
    }

    @Override
    public List<SelectContact> getContacts() {
        mContacts = super.getContacts();
        return mContacts;
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
    public class SelectContactQueryHandler extends QueryHandler {

        public SelectContactQueryHandler(Context context, Callbacks callbacks) {
            super(context, callbacks);
        }


        @Override
        public void onContactQueryComplete(Cursor cursor) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    String name = cursor.getString(DISPLAY_NAME_INDEX);
                    Long id = cursor.getLong(CONTACT_ID_INDEX);
                    SelectContact contact = new SelectContact();
                    contact.setName(name);
                    contact.setId(id);
                    contact.setChecked(isPreviousSelected(id));
                    Uri.Builder builder = ContactsContract.Contacts.CONTENT_URI.buildUpon();
                    ContentUris.appendId(builder, cursor.getLong(CONTACT_ID_INDEX));
                    builder.appendEncodedPath(ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                    Uri phoneNumbersUri = builder.build();

                    startQuery(TOKEN_PHONE,
                            contact,
                            phoneNumbersUri,
                            PHONE_PROJECTION,
                            ContactsContract.CommonDataKinds.Phone.MIMETYPE + "=?",
                            new String[] {ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                            null);

                    cursor.moveToNext();
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }


    }

}
