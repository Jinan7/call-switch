package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

public class ContactLabHelper<T extends Contact> {

    private Class<T> clazz;


    private SQLiteDatabase mDatabase;


    private Context mContext;
    private List<T> mContacts;
    private List<T> mSearchResults;
    private List<T> mPhoneBookImage;


    public ContactLabHelper(Context context, Class<T> clazz) {
        mContext = context.getApplicationContext();
        mDatabase = new DBOpenHelper(mContext).getWritableDatabase();
        mContacts = new ArrayList<>();
        mSearchResults = new ArrayList<>();
        mPhoneBookImage = new ArrayList<>();

        this.clazz = clazz;


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
        contacts = getContacts(cursor);
        return contacts;
    }

    @SuppressWarnings("unchecked")
    public List<T> getContacts(String searchQuery) {

        List<T> contacts = new ArrayList<>();

        if (searchQuery.isEmpty()) {
            return contacts;
        }
        String query = "%" + searchQuery + "%";
        ContactCursorWrapper<T> cursor = queryDatabase(Schema.Contact.Cols.name + " LIKE ?", new String[] { query }, Schema.Contact.Cols.name + " ASC");
        contacts = getContacts(cursor);
        return contacts;
    }

    public List<T> getContacts(ContactCursorWrapper<T> cursor) {

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

    public boolean isDeleted(Contact contact) {

        for (T _contact : mPhoneBookImage) {
            if (_contact.getId().equals(contact.getId())) {
                return false;
            }
        }

        return true;
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

    public void delete(Context context, Contact contact) {
        mDatabase.delete(Schema.Contact.name, Schema.Contact.Cols.id + " = ?", new String [] { contact.getId().toString()});

        List<Reply> replies = new ArrayList<>();

        for (Reply reply : replies) {
            reply.deleteContact(contact);
            ReplyLab.getInstance(context).update(context, reply);
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
        values.put(Schema.Contact.Cols.name, contact.getName());
        values.put(Schema.Contact.Cols.phone, contact.getPhone());
        if (contact.getActiveReplyId() != null)  values.put(Schema.Contact.Cols.active_reply, contact.getActiveReplyId().toString());
        else values.put(Schema.Contact.Cols.active_reply, "");
        values.put(Schema.Contact.Cols.replies, new Gson().toJson(contact.getReplies()));
        values.put(Schema.Contact.Cols.color, contact.getColor());
        values.put(Schema.Contact.Cols.secondary_color, contact.getSecondaryColor());
        return values;
    }



    public List<T> getSearchResults() {
        return mSearchResults;
    }


    public void addContactToPhoneImage(T contact) {
        mPhoneBookImage.add(contact);
    }


}
