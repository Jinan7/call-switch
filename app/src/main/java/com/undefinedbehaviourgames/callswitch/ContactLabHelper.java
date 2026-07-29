package com.undefinedbehaviourgames.callswitch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.transition.Scene;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;

import database.ContactCursorWrapper;
import database.DBOpenHelper;
import database.Schema;

public class ContactLabHelper<T extends Contact> {

    private Class<T> clazz;


    private SQLiteDatabase mDatabase;


    protected Context mContext;
    private List<T> mContacts;
    private List<T> mSearchResults;
    private HashSet<String> mPhoneBookImage;


    public ContactLabHelper(Context context, Class<T> clazz) {
        mContext = context.getApplicationContext();
        mDatabase = new DBOpenHelper(mContext).getWritableDatabase();
        mContacts = new ArrayList<>();
        mSearchResults = new ArrayList<>();
        mPhoneBookImage = new HashSet<>();

        this.clazz = clazz;


    }



    @SuppressWarnings("unchecked")
    public T get(String lookupkey) {

        synchronized (this) {
            ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.lookupKey + " = ?", new String [] {lookupkey}, null);
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

    }

    @SuppressWarnings("unchecked")
    public T get(PhoneNumber phoneNumber) {

        synchronized (this) {
            String phoneNumberString = new Gson().toJson(phoneNumber);
            ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.phone_proto + " = ?", new String[] { phoneNumberString }, null);
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

    }

    @SuppressWarnings("unchecked")
    public List<T> getContacts() {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();
            ContactCursorWrapper<T> cursor = queryDatabase(null, null, Schema.Contact.Cols.name + " ASC");
            contacts = getContacts(cursor);
            return contacts;
        }


    }

    public List<T> getContacts(WeakReference<Callbacks<T>> callbacksWeakReference) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();
            ContactCursorWrapper<T> cursor = queryDatabase(null, null, Schema.Contact.Cols.name + " ASC");
            contacts = getContacts(cursor, callbacksWeakReference);
            return contacts;
        }

    }

    @SuppressWarnings("unchecked")
    public List<T> getContacts(String searchQuery) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();

            if (searchQuery.isEmpty()) {
                return contacts;
            }
            String query = "%" + searchQuery + "%";
            ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.name + " LIKE ?", new String[] { query }, Schema.Contact.Cols.name + " ASC");
            contacts = getContacts(cursor);
            return contacts;
        }

    }

    public List<T> getContacts(String searchQuery, WeakReference<SearchCallbacks<T>> callbacksWeakReference) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();

            if (searchQuery.isEmpty()) {
                if (callbacksWeakReference.get() != null) {
                    callbacksWeakReference.get().onSearchResults(contacts);
                }
                return contacts;
            }
            String query = "%" + searchQuery + "%";
            ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.name + " LIKE ?", new String[] { query }, Schema.Contact.Cols.name + " ASC");
            contacts = getContacts(cursor, callbacksWeakReference, null);
            return contacts;
        }

    }

    public List<T> getContacts(ContactCursorWrapper<T> cursor, WeakReference<SearchCallbacks<T>> callbacksWeakReference, SearchCallbacks callbacks) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();

            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    T contact = (T) cursor.getContact();
                    //only check if contact has been deleted once contact query handler fetches all contacts
                    //if not any contact that has not yet been fetched will be marked as deleted temporarily since
                    //it will not be in the list of contacts
                    if (ContactQueryHandler.getInstance(mContext).getQueryState() == State.FETCHED) contact.setDeleted(isDeleted(contact));
                    contacts.add(contact);

                    cursor.moveToNext();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                cursor.close();
            }

            if (callbacksWeakReference.get() != null) {
                callbacksWeakReference.get().onSearchResults(contacts);
            }
            return contacts;
        }

    }
    public List<T> getContacts(ContactCursorWrapper<T> cursor, WeakReference<Callbacks<T>> callbacksWeakReference) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();

            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    T contact = (T) cursor.getContact();
                    //only check if contact has been deleted once contact query handler fetches all contacts
                    //if not any contact that has not yet been fetched will be marked as deleted temporarily since
                    //it will not be in the list of contacts
                    if (ContactQueryHandler.getInstance(mContext).getQueryState() == State.FETCHED) contact.setDeleted(isDeleted(contact));
                    contacts.add(contact);

                    if (callbacksWeakReference.get() != null) {
                        callbacksWeakReference.get().onGetSingleContact(contact);
                    }
                    cursor.moveToNext();
                }

                final List<T> immutableContactSnapshot = new ArrayList<>(contacts);

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
    public List<T> getContacts(ContactCursorWrapper<T> cursor) {

        synchronized (this) {
            List<T> contacts = new ArrayList<>();
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    T contact = (T) cursor.getContact();
                    //only check if contact has been deleted once contact query handler fetches all contacts
                    //if not any contact that has not yet been fetched will be marked as deleted temporarily since
                    //it will not be in the list of contacts
                    if (ContactQueryHandler.getInstance(mContext).getQueryState() == State.FETCHED) contact.setDeleted(isDeleted(contact));
                    contacts.add(contact);
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

    }

    public boolean isDeleted(Contact contact) {
        return !mPhoneBookImage.contains(contact.getLookupKey());
    }

    public void removeDeleted() {
        if (ContactQueryHandler.getInstance(mContext).getQueryState() != State.FETCHED) return;
        List<T> contacts = getContacts();

        for (T contact : contacts) {
            if (isDeleted(contact)) {
                delete(mContext, contact);
            }
        }

    }
    public void add(Contact contact) {

        synchronized (this) {
            ContentValues values = getContentValues(contact);
            mDatabase.insert(
                    Schema.Contact.name,
                    null,
                    values
            );
        }


    }

    public void update(Contact contact) {

        synchronized (this) {
            ContentValues values = getContentValues(contact);

            mDatabase.update(
                    Schema.Contact.name,
                    values,
                    Schema.Contact.Cols.lookupKey + " = ?",
                    new String [] { contact.getLookupKey()}
            );
        }

    }

    public void delete(Context context, Contact contact) {

        synchronized (this) {
            mDatabase.delete(Schema.Contact.name, Schema.Contact.Cols.lookupKey + " = ?", new String [] { contact.getLookupKey()});

            List<Reply> replies = contact.getReplies(context);

            for (Reply reply : replies) {
                reply.deleteContact(contact);
                ReplyLab.getInstance(context).update(context, reply);
            }
        }

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
        values.put(Schema.Contact.Cols.lookupKey, contact.getLookupKey());
        values.put(Schema.Contact.Cols.name, contact.getName());
        values.put(Schema.Contact.Cols.phone, contact.getPhone());
        if (contact.getActiveReplyId() != null)  values.put(Schema.Contact.Cols.active_reply, contact.getActiveReplyId().toString());
        else values.put(Schema.Contact.Cols.active_reply, "");
        values.put(Schema.Contact.Cols.replies, new Gson().toJson(contact.getReplies()));
        values.put(Schema.Contact.Cols.phone_proto, new Gson().toJson(contact.getPhoneNumber()));
        values.put(Schema.Contact.Cols.color, contact.getColor());
        values.put(Schema.Contact.Cols.secondary_color, contact.getSecondaryColor());
        return values;
    }



    public void addContactToPhoneImage(T contact) {
        mPhoneBookImage.add(contact.getLookupKey());
    }

    public interface Callbacks<T extends Contact> {
        void onGetSingleContact(T contact);
        void onGetAllContacts(List<T> contacts);
    }

    public interface SearchCallbacks<T extends Contact> {
        void onSearchResults(List<T> contacts);
    }

}
