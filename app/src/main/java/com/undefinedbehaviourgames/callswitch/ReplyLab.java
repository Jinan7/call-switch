package com.undefinedbehaviourgames.callswitch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import database.DBOpenHelper;

public class ReplyLab {

    private SQLiteDatabase mDatabase;
    private static ReplyLab sReplyLab;
    private List<Reply> mReplies;
    private ReplyLab(Context context) {
        mDatabase = new DBOpenHelper(context.getApplicationContext()).getWritableDatabase();
        mReplies = new ArrayList<>();
    };

    public static ReplyLab getInstance(Context context) {
        if (sReplyLab == null) {
            sReplyLab = new ReplyLab(context);
        }

        return sReplyLab;
    }

    public Reply get(UUID id) {

        for (Reply reply : mReplies) {
            if (reply.getId().equals(id)) {
                return reply;
            }
        }

        return null;
    }
    public List<Reply> getReplies() {
        return mReplies;
    }

    public void add(Reply reply) {
        mReplies.add(reply);
    }
    public void update(Reply reply) {}
}
