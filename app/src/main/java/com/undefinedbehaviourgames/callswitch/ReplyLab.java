package com.undefinedbehaviourgames.callswitch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.DBOpenHelper;
import database.ReplyCursorWrapper;
import database.Schema;
import database.Schema.Reply.Cols;

public class ReplyLab {

    private SQLiteDatabase mDatabase;
    private static ReplyLab sReplyLab;
    private ReplyLab(Context context) {
        mDatabase = new DBOpenHelper(context.getApplicationContext()).getWritableDatabase();
    };

    public static ReplyLab getInstance(Context context) {
        if (sReplyLab == null) {
            sReplyLab = new ReplyLab(context);
        }

        return sReplyLab;
    }

    public Reply get(UUID id) {

        if (id == null) return null;
        ReplyCursorWrapper cursor = queryDatabase( Cols.uuid + " = ?", new String []  { id.toString() });
        Reply reply;
        try {

            cursor.moveToFirst();
            reply = cursor.getReply();
        } finally {
            cursor.close();
        }

        return reply;
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

    public void add(Context context, Reply reply) {

        ContentValues values = getContentValues(reply);

        mDatabase.insert(Schema.Reply.name, null, values);

        //if reply was added successfully, go through reply to list and add reply to each contact and update active reply based on reply priority
        //make asynchronous
        Reply newReply = get(reply.getId());

        //if null, reply was not added successfully
        if (newReply != null) {
            List<Contact> replyToList = newReply.getReplyToList();

            for (Contact contact : replyToList) {
                //add the new reply to the list of contact replies
                contact.addReply(newReply);
                //update active reply if necessary
                contact.updateActiveReply(context, newReply);
                //write to database
                ContactLab.getInstance(context).update(contact);
            }
        }
    }
    public void update(Reply reply) {
        ContentValues values = getContentValues(reply);
        mDatabase.update(Schema.Reply.name, values, Cols.uuid + " = ?", new String[] {reply.getId().toString()});
    }


    public ContentValues getContentValues(Reply reply) {
        ContentValues values = new ContentValues();

        values.put(Cols.uuid, reply.getId().toString());
        values.put(Cols.message, reply.getReply());
        if (reply.isEnabled()) values.put(Cols.enabled, 1); else values.put(Cols.enabled, 0);
        values.put(Cols.reply_to_list, new Gson().toJson(reply.getReplyToList()));
        if (reply.replyUnknown()) values.put(Cols.reply_unknown, 1); else values.put(Cols.reply_unknown, 0);
        values.put(Cols.priority, reply.getPriority().ordinal());

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
}
