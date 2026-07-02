package database;

import static database.Schema.Reply.*;

import android.database.Cursor;
import android.database.CursorWrapper;
import android.renderscript.RenderScript;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.undefinedbehaviourgames.callswitch.Contact;
import com.undefinedbehaviourgames.callswitch.Reply;

import java.util.ArrayList;
import java.util.UUID;

public class ReplyCursorWrapper extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */
    public ReplyCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Reply getReply() {

        Reply reply = new Reply();
        String uuidString = getString(getColumnIndex(Cols.uuid));
        String message = getString(getColumnIndex(Cols.message));
        int enabled = getInt(getColumnIndex(Cols.enabled));
        String replyToListString = getString(getColumnIndex(Cols.reply_to_list));
        int replyUnknown = getInt(getColumnIndex(Cols.reply_unknown));
        int priorityOrdinal = getInt(getColumnIndex(Cols.priority));

        ArrayList<Contact> replyToList = new Gson().fromJson(replyToListString, new TypeToken<ArrayList<Contact>>() {}.getType());

        reply.setId(UUID.fromString(uuidString));
        reply.setReply(message);
        if (enabled == 1) reply.setEnabled(true); else reply.setEnabled(false);
        reply.setReplyToList(replyToList);
        if (replyUnknown == 1) reply.setReplyUnknown(true); else reply.setReplyUnknown(false);
        reply.setPriority(priorityOrdinal);

        return reply;
    }
}
