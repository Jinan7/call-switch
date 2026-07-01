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

public class SelectContactLab {

    public static final int TOKEN_CONTACT = 0;
    public static final int TOKEN_PHONE = 1;
    public static final int TOKEN_SEARCH_CONTACT = 2;
    public static final int TOKEN_SEARCH_PHONE = 3;
    private final int DISPLAY_NAME_INDEX = 1;
    private final int PHONE_INDEX = 1;
    private final int CONTACT_ID_INDEX = 0;
    private WeakReference<Callbacks> mCallbacks;
    private final String[] CONTACT_PROJECTION = new String [] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
    };
    private final String[] PHONE_PROJECTION = new String [] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private Context mContext;
    private List<SelectContact> mContacts;
    private List<SelectContact> mSearchResults;
    private List<Contact> mPreviousSelectedContacts;
    private QueryHandler mHandler;
    public SelectContactLab(Context context, Callbacks callbacks) {
        mContext = context.getApplicationContext();
        mHandler = new QueryHandler(mContext);
        mCallbacks = new WeakReference<>(callbacks);
        mContacts = new ArrayList<>();
        mSearchResults = new ArrayList<>();
        mPreviousSelectedContacts = new ArrayList<>();
    }

    public void startQuery() {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mHandler.startQuery(TOKEN_CONTACT,
                null,
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    public void startSearchQuery(String searchQuery) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (searchQuery.isEmpty()) {
            mSearchResults.clear();
            Callbacks callbacks = mCallbacks.get();
            if (callbacks != null) callbacks.onSearchComplete();
            return;
        }
        String query = "%" + searchQuery + "%";
        mHandler.startQuery(TOKEN_SEARCH_CONTACT,
                null,
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
                new String[] { query },
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    public SelectContact get(Long id) {

        //make asynchronous
        for (SelectContact contact : mContacts) {
            if (contact.getId().equals(id)) {
                return contact;
            }
        }

        return null;
    }
    public List<SelectContact> getContacts() {
        return mContacts;
    }

    public List<SelectContact> getSearchResults() {
        return mSearchResults;
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
    public void cancel() {
        mHandler.cancelOperation(TOKEN_CONTACT);
        mHandler.cancelOperation(TOKEN_PHONE);
    }
    private class QueryHandler extends AsyncQueryHandler {

        public QueryHandler(Context context) {
            super(context.getContentResolver());
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            super.onQueryComplete(token, cookie, cursor);

            switch (token) {

                case TOKEN_CONTACT:
                    mContacts.clear();
                    Callbacks callbacks = mCallbacks.get();
                    if (callbacks != null) callbacks.onQueryComplete();
                    onContactQueryComplete(cursor);
                    break;

                case TOKEN_PHONE:
                    onPhoneQueryComplete(cursor, (SelectContact) cookie);
                    break;
                case TOKEN_SEARCH_CONTACT:
                    mSearchResults.clear();
                    Callbacks _callbacks = mCallbacks.get();
                    if (_callbacks != null) _callbacks.onSearchComplete();
                    onContactSearchQueryComplete(cursor);
                    break;
                default:
                    break;
            }
        }

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

                    mHandler.startQuery(TOKEN_PHONE,
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

        public void onContactSearchQueryComplete(Cursor cursor) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Long id = cursor.getLong(CONTACT_ID_INDEX);
                    SelectContact contact = get(id);

                    if (contact != null) {
                        mSearchResults.add(contact);
                        Callbacks callbacks = mCallbacks.get();

                        if (callbacks != null) {
                            callbacks.onSearchComplete();
                        }
                    }
                    cursor.moveToNext();
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        public void onPhoneQueryComplete(Cursor cursor, SelectContact contact) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();
                String phone = cursor.getString(PHONE_INDEX);
                contact.setPhone(phone);
                mContacts.add(contact);
                Callbacks callbacks = mCallbacks.get();

                if (callbacks != null) {
                    callbacks.onQueryComplete();
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }

//        public void onSearchPhoneQueryComplete(Cursor cursor, SelectContact contact) {
//            try {
//                if (cursor.getCount() == 0) return;
//                cursor.moveToFirst();
//                String phone = cursor.getString(PHONE_INDEX);
//                contact.setPhone(phone);
//                mSearchResults.add(contact);
//                Callbacks callbacks = mCallbacks.get();
//
//                if (callbacks != null) {
//                    callbacks.onSearchComplete();
//                }
//            } finally {
//                if (cursor != null) cursor.close();
//            }
//        }
    }

    public interface Callbacks {
        void onQueryComplete();
        void onSearchComplete();
    }
}
