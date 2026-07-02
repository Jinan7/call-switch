package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "call switch";
    private static final int VERSION = 1;
    public DBOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //create contacts table
        db.execSQL(
                "create table " + Schema.Contact.name +
                        "(" +
                        "_id integer primary key autoincrement, " +
                        Schema.Contact.Cols.id + " UNIQUE, " +
                        Schema.Contact.Cols.name + " COLLATE NOCASE, " +
                        Schema.Contact.Cols.phone + ", " +
                        Schema.Contact.Cols.active_reply + ", " +
                        Schema.Contact.Cols.replies +
                        ")"
        );

        //create replies table
        db.execSQL(
                "create table " +
                        Schema.Reply.name +
                        "(" +
                        "_id integer primary key autoincrement, " +
                        Schema.Reply.Cols.uuid + " UNIQUE, " +
                        Schema.Reply.Cols.priority + ", " +
                        Schema.Reply.Cols.message + ", " +
                        Schema.Reply.Cols.enabled + ", " +
                        Schema.Reply.Cols.reply_to_list + ", " +
                        Schema.Reply.Cols.replace_equal_priority + ", " +
                        Schema.Reply.Cols.reply_unknown +
                        ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
