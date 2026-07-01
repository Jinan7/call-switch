package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ContactLabHelper<T extends Contact> {

    public static final int TOKEN_CONTACT = 0;
    public static final int TOKEN_PHONE = 1;
    public static final int TOKEN_SEARCH_CONTACT = 2;
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
    private List<T> mContacts;
    private List<T> mSearchResults;

    public ContactLabHelper(Context context) {
        mContext = context.getApplicationContext();
        mContacts = new ArrayList<>();
        mSearchResults = new ArrayList<>();
    }

    public void startQuery(Callbacks callbacks) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        new QueryHandler(mContext, callbacks).startQuery(TOKEN_CONTACT,
                null,
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    public void startSearchQuery(String searchQuery, Callbacks callbacks) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (searchQuery.isEmpty()) {
            mSearchResults.clear();
            if (callbacks != null) callbacks.onSearchComplete();
            return;
        }
        String query = "%" + searchQuery + "%";
        new QueryHandler(mContext, callbacks).startQuery(TOKEN_SEARCH_CONTACT,
                null,
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                ContactsContract.Contacts.DISPLAY_NAME + " LIKE ?",
                new String[] { query },
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");
    }

    public T get(Long id) {

        //make asynchronous
        for (T contact : mContacts) {
            if (contact.getId().equals(id)) {
                return contact;
            }
        }

        return null;
    }
    public List<T> getContacts() {
        return mContacts;
    }

    public List<T> getSearchResults() {
        return mSearchResults;
    }


    private class QueryHandler extends AsyncQueryHandler {



        private WeakReference<Callbacks> mCallbacks;
        public QueryHandler(Context context, Callbacks callbacks) {
            super(context.getContentResolver());
            mCallbacks = new WeakReference(callbacks);
        }
        @Override
        @SuppressWarnings("unchecked")
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
                    onPhoneQueryComplete(cursor, (T) cookie);
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
                    Contact contact = new Contact();
                    contact.setName(name);
                    contact.setId(id);
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

        public void onContactSearchQueryComplete(Cursor cursor) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    Long id = cursor.getLong(CONTACT_ID_INDEX);
                    T contact = get(id);

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
        public void onPhoneQueryComplete(Cursor cursor, T contact) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();
                String phone = cursor.getString(PHONE_INDEX);
                contact.setPhone(phone);
                mContacts.add(contact);
                Callbacks callbacks = mCallbacks.get();

                if (callbacks != null) {
                    Log.d("Debug", contact.getName());
                    Log.d("Debug", String.valueOf(mContacts.size()));
                    callbacks.onQueryComplete();
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }


    }

    public interface Callbacks {
        void onQueryComplete();
        void onSearchComplete();
    }
}
