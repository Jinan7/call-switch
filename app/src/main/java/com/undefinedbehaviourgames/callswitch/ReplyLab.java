package com.undefinedbehaviourgames.callswitch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telecom.Call;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import database.DBOpenHelper;
import database.ReplyCursorWrapper;
import database.Schema;
import database.Schema.Reply.Cols;

public class ReplyLab {

    private SQLiteDatabase mDatabase;
    private static volatile ReplyLab sReplyLab;
    private boolean unreachableUpdate;
    private ReplyLab(Context context) {
        mDatabase = new DBOpenHelper(context.getApplicationContext()).getWritableDatabase();
        unreachableUpdate = false;
    };

    public static ReplyLab getInstance(Context context) {
        if (sReplyLab == null) {

            synchronized (ReplyLab.class) {
                if (sReplyLab == null) {
                    sReplyLab = new ReplyLab(context);
                }

            }

        }

        return sReplyLab;
    }

    public boolean hasUnreachableUpdate() {
        return unreachableUpdate;
    }

    public void setUnreachableUpdate(boolean unreachableUpdate) {
        this.unreachableUpdate = unreachableUpdate;
    }

    public Reply get(UUID id) {

        synchronized (ReplyLab.this) {
            if (id == null) return null;
            ReplyCursorWrapper cursor = queryDatabase( Cols.uuid + " = ?", new String []  { id.toString() });
            Reply reply;
            try {
                if (cursor.getCount() != 0)
                {
                    cursor.moveToFirst();
                    reply = cursor.getReply();
                } else {
                    reply = null;
                }

            } finally {
                cursor.close();
            }

            return reply;
        }

    }
    public List<Reply> getReplies() {

        ReplyCursorWrapper cursor = queryDatabase(null, null) ;
        List<Reply> replies = new ArrayList<>();

        try {
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                replies.add(cursor.getReply());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return replies;
    }
    public List<Reply> getReplies(WeakReference<Callbacks> callbacksWeakReference) {

        synchronized (ReplyLab.this) {
            ReplyCursorWrapper cursor = queryDatabase(null, null) ;
            List<Reply> replies = new ArrayList<>();

            try {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {

                    replies.add(cursor.getReply());

                    cursor.moveToNext();
                }

                List<Reply> repliesImage = new ArrayList<>(replies);
                if (callbacksWeakReference.get() != null) {
                    callbacksWeakReference.get().onGetAllReplies(repliesImage);
                }
            } finally {
                cursor.close();
            }
            return replies;
        }

    }

    public List<Reply> getReplies(WeakReference<Callbacks> callbacksWeakReference, Future<Object> future) {

        synchronized (ReplyLab.this) {
            ReplyCursorWrapper cursor = queryDatabase(null, null) ;
            List<Reply> replies = new ArrayList<>();

            try {
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    if (future.isCancelled()) break;

                    replies.add(cursor.getReply());

                    cursor.moveToNext();
                }

                List<Reply> repliesImage = new ArrayList<>(replies);
                if (!future.isCancelled() && callbacksWeakReference.get() != null) {
                    callbacksWeakReference.get().onGetAllReplies(repliesImage);
                }
            } finally {
                cursor.close();
            }
            return replies;
        }

    }

    public void add(Context context, Reply reply) {

        synchronized (ReplyLab.this) {
            ContentValues values = getContentValues(reply);

            mDatabase.insert(Schema.Reply.name, null, values);

            //if reply was added successfully, go through reply to list and add reply to each contact and update active reply based on reply priority
            //make asynchronous
            Reply newReply = get(reply.getId());

            //if null, reply was not added successfully
            if (newReply != null) {

                List<Contact> replyToList = newReply.getReplyToList(context);

                for (Contact contact : replyToList) {
                    //add the new reply to the list of contact replies
                    contact.addReply(newReply);
                    //update active reply if necessary
                    contact.updateActiveReply(context, newReply, newReply.replaceEqualPriority());
                    //write to database
                    ContactLab.getInstance(context).update(contact);
                }

                if (newReply.replyUnknown()) {
                    Contact contact = ContactPreferences.getUnknownContact(context);
                    contact.addReply(newReply);
                    contact.updateActiveReply(context, newReply, newReply.replaceEqualPriority());
                    ContactPreferences.setUnknownContact(context, contact);
                }
            }
        }

    }

    public void update(Reply reply) {
        ContentValues values = getContentValues(reply);
        mDatabase.update(Schema.Reply.name, values, Cols.uuid + " = ?", new String[] {reply.getId().toString()});
    }
    public void update(Context context, Reply reply) {

        //this is a very brute force solution
        //consider getting prevReplytoList and newReplytoList
        //compare both list to get the contacts that have been removed and the contacts that have been newly added
        //for removed contacts, call contact.removeReply;
        //for added contacts, call contact.addReply();

        //before update, get prev reply to list
        //remove reply from all contacts in the list
        //after update is successful
        //and then add reply to all the contacts in new reply to list
        //the reply argument reply to list cannot be used because it has already been tampered with
        //so query database for the current state before update

        synchronized (ReplyLab.this) {
            List<Contact> prevReplyToList = get(reply.getId()).getReplyToList(context);
            boolean prevReplyUnknown = get(reply.getId()).replyUnknown();

            ContentValues values = getContentValues(reply);
            mDatabase.update(Schema.Reply.name, values, Cols.uuid + " = ?", new String[] {reply.getId().toString()});

            //if updated successfully, go through reply list and update active reply based on priority and only if reply is enabled
            //make asynchronous
            Reply updatedReply = get(reply.getId());



            if (updatedReply != null) {
                //first go through previous reply to list and remove reply from contacts in the list
                for (Contact contact : prevReplyToList) {
                    //remove the reply from contacts reply to list
                    contact.removeReply(context, updatedReply);
                    //update contact
                    ContactLab.getInstance(context).update(contact);
                }
                List<Contact> replyToList = updatedReply.getReplyToList(context);
                for (Contact contact : replyToList) {
                    //add the new reply to the list of contact replies
                    contact.addReply(updatedReply);
                    //if reply was previously active or is enabled, then update active reply if necessary
                    //if reply was previously active, it will be restored since the first remove reply for loop would
                    //have made the contacts active reply to be null
                    contact.updateActiveReply(context, updatedReply, updatedReply.replaceEqualPriority());
                    //write to database
                    ContactLab.getInstance(context).update(contact);
                }

                //update unknown contacts
                Contact contact = ContactPreferences.getUnknownContact(context);

                if (updatedReply.replyUnknown()) {
                    contact.addReply(updatedReply);
                    contact.updateActiveReply(context, updatedReply, updatedReply.replaceEqualPriority());

                } else {
                    contact.removeReply(context, updatedReply);
                }

                ContactPreferences.setUnknownContact(context, contact);
            }
        }

    }

    public void delete(Context context, Reply reply) {

        synchronized (ReplyLab.this) {
            mDatabase.delete(Schema.Reply.name, Cols.uuid + " = ?", new String[] { reply.getId().toString()});
            List<Contact> replyToList = reply.getReplyToList(context);

            for (Contact contact : replyToList) {
                contact.removeReply(context, reply);
                ContactLab.getInstance(context).update(contact);
            }
        }

    }

    public ContentValues getContentValues(Reply reply) {
        ContentValues values = new ContentValues();

        values.put(Cols.uuid, reply.getId().toString());
        values.put(Cols.message, reply.getReply());
        if (reply.isEnabled()) values.put(Cols.enabled, 1); else values.put(Cols.enabled, 0);
        values.put(Cols.reply_to_list, new Gson().toJson(reply.getReplyToList()));
        if (reply.replyUnknown()) values.put(Cols.reply_unknown, 1); else values.put(Cols.reply_unknown, 0);
        values.put(Cols.priority, reply.getPriority().ordinal());
        if (reply.replaceEqualPriority()) values.put(Cols.replace_equal_priority, 1); else values.put(Cols.replace_equal_priority, 0);
        return values;
    }
    public ReplyCursorWrapper queryDatabase(String queryString, String [] queryArgs) {

        Cursor cursor = mDatabase.query(
                Schema.Reply.name,
                null,
                queryString,
                queryArgs,
                null,
                null,
                null,
                null
        );

        return new ReplyCursorWrapper(cursor);
    }

    public void setEnabledAllReplies(Context context, boolean isChecked) {

        List<Reply> replies = getReplies();

        for (Reply reply : replies) {
            reply.setEnabled(isChecked);
            update(context, reply);
        }
    }

    public void setEnabledAllReplies(Context context, boolean isChecked, WeakReference<Callbacks> callbacksWeakReference) {

        List<Reply> replies = getReplies();

        int i = 0;
        for (Reply reply : replies) {
            reply.setEnabled(isChecked);
            update(context, reply);
            i+=1;
        }

        if (callbacksWeakReference.get() != null) {
            callbacksWeakReference.get().onUpdateReplies(replies);
        }
    }

    public interface Callbacks {
        void ongetSingleReply(Reply reply);
        void onGetAllReplies(List<Reply> replies);
        void onUpdateReplies(List<Reply> replies);
        void onUpdateReply(int index);
    }
}
