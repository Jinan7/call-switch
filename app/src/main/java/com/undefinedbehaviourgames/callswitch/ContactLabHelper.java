package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import database.ContactCursorWrapper;
import database.DBOpenHelper;
import database.Schema;

public class ContactLabHelper<T extends Contact, U extends ContactLabHelper.QueryHandler> {

    private Class<T> clazz;
    private Class<U> qClazz;
    public static final int TOKEN_CONTACT = 0;
    public static final int TOKEN_PHONE = 1;
    public static final int TOKEN_SEARCH_CONTACT = 2;
    public static final int TOKEN_LAST_PHONE = 3;
    public final int DISPLAY_NAME_INDEX = 1;
    public final int PHONE_INDEX = 1;
    public final int CONTACT_ID_INDEX = 0;
    private SQLiteDatabase mDatabase;
    private WeakReference<Callbacks> mCallbacks;
    private final String[] CONTACT_PROJECTION = new String [] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
    };
    protected final String[] PHONE_PROJECTION = new String [] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };
    private Context mContext;
    private List<T> mContacts;
    private List<T> mSearchResults;

    public ContactLabHelper(Context context, Class<T> clazz, Class<U> qClazz) {
        mContext = context.getApplicationContext();
        mDatabase = new DBOpenHelper(mContext).getWritableDatabase();
        mContacts = new ArrayList<>();
        mSearchResults = new ArrayList<>();
        this.clazz = clazz;
        this.qClazz = qClazz;
    }

    public void startQuery(Callbacks callbacks) {
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        try {

            U handler = qClazz.getDeclaredConstructor(qClazz.getDeclaringClass() , Context.class, Callbacks.class).newInstance(this, mContext, callbacks);
            handler.startQuery(TOKEN_CONTACT,
                    null,
                    ContactsContract.Contacts.CONTENT_URI,
                    CONTACT_PROJECTION,
                    ContactsContract.Contacts.HAS_PHONE_NUMBER,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException e) {

            java.lang.reflect.Constructor<?>[] constructors = qClazz.getDeclaredConstructors();

            for (java.lang.reflect.Constructor<?> constructor : constructors) {
                Log.d("Constructor found", constructor.toGenericString());
            }

            throw new RuntimeException(e);
        }


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

    @SuppressWarnings("unchecked")
    public T get(Long id) {

        //make asynchronous
        String _id = id.toString();
        ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.id + " = ?", new String [] {id.toString()}, null);
        T contact;
        try {
            cursor.moveToFirst();
            contact = (T) cursor.getContact();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            cursor.close();
        }

        return contact;
    }

    @SuppressWarnings("unchecked")
    public List<T> getContacts() {

        List<T> contacts = new ArrayList<>();
        ContactCursorWrapper<T> cursor = queryDatabase(null, null, Schema.Contact.Cols.name + " ASC");

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                contacts.add((T) cursor.getContact());
                cursor.moveToNext();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            cursor.close();
        }

        return contacts;
    }

    public void add(Contact contact) {

        ContentValues values = getContentValues(contact);


            mDatabase.insert(
                    Schema.Contact.name,
                    null,
                    values
            );

    }

    public void update(Contact contact) {
        ContentValues values = getContentValues(contact);

        mDatabase.update(
                Schema.Contact.name,
                values,
                Schema.Contact.Cols.id + " = ?",
                new String [] { contact.getId().toString()}
        );
    }

    public ContactCursorWrapper queryDatabase(String queryString, String [] queryArgs, String orderBy ) {

        Cursor cursor = mDatabase.query(
                Schema.Contact.name,
                null,
                queryString,
                queryArgs,
                null,
                null,
                orderBy,
                null
        );

        return new ContactCursorWrapper(cursor, clazz);
    }

    public ContentValues getContentValues(Contact contact) {

        ContentValues values = new ContentValues();
        values.put(Schema.Contact.Cols.id, contact.getId().toString());
        values.put(Schema.Contact.Cols.name, contact.getName());
        values.put(Schema.Contact.Cols.phone, contact.getPhone());
        if (contact.getActiveReplyId() != null)  values.put(Schema.Contact.Cols.active_reply, contact.getActiveReplyId().toString());
        else values.put(Schema.Contact.Cols.active_reply, "");
        values.put(Schema.Contact.Cols.replies, new Gson().toJson(contact.getReplies()));
        return values;
    }



    public List<T> getSearchResults() {
        return mSearchResults;
    }


    public class QueryHandler extends AsyncQueryHandler {



        private WeakReference<Callbacks> mCallbacks;
        public QueryHandler(Context context, Callbacks callbacks) {
            super(context.getContentResolver());
            mCallbacks = new WeakReference<Callbacks>(callbacks);
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
                    onPhoneQueryComplete(cursor, (T) cookie, false);
                    break;
                case TOKEN_SEARCH_CONTACT:
                    mSearchResults.clear();
                    Callbacks _callbacks = mCallbacks.get();
                    if (_callbacks != null) _callbacks.onSearchComplete();
                    onContactSearchQueryComplete(cursor);
                    break;
                case TOKEN_LAST_PHONE:
                    onPhoneQueryComplete(cursor, (T) cookie, true);
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
                    T contact = clazz.getDeclaredConstructor().newInstance();
                    contact.setName(name);
                    contact.setId(id);

                    Uri.Builder builder = ContactsContract.Contacts.CONTENT_URI.buildUpon();
                    ContentUris.appendId(builder, cursor.getLong(CONTACT_ID_INDEX));
                    builder.appendEncodedPath(ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                    Uri phoneNumbersUri = builder.build();

                    int token = cursor.isLast() ? TOKEN_LAST_PHONE : TOKEN_PHONE;
                    startQuery(token,
                            contact,
                            phoneNumbersUri,
                            PHONE_PROJECTION,
                            ContactsContract.CommonDataKinds.Phone.MIMETYPE + "=?",
                            new String[] {ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                            null);

                    cursor.moveToNext();
                }
            } catch (InvocationTargetException | InstantiationException | NoSuchMethodException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
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
        public void onPhoneQueryComplete(Cursor cursor, T contact, boolean last) {
            try {
                if (cursor.getCount() == 0) return;
                cursor.moveToFirst();
                String phone = cursor.getString(PHONE_INDEX);
                contact.setPhone(phone);
                add(contact);
                Callbacks callbacks = mCallbacks.get();

                if (last && callbacks != null) {
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
