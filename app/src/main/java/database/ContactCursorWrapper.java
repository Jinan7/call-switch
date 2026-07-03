package database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.undefinedbehaviourgames.callswitch.Contact;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

import database.Schema.Contact.Cols;

public class ContactCursorWrapper<T extends Contact> extends CursorWrapper {
    /**
     * Creates a cursor wrapper.
     *
     * @param cursor The underlying cursor to wrap.
     */

    private Class<T> clazz;
    public ContactCursorWrapper(Cursor cursor, Class<T> clazz) {
        super(cursor);
        this.clazz = clazz;
    }

    public T getContact() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {

        T contact = clazz.getDeclaredConstructor().newInstance();

        Long id = getLong(getColumnIndex(Cols.id));
        String name = getString(getColumnIndex(Cols.name));
        String phone = getString(getColumnIndex(Cols.phone));
        String activeReplyId = getString(getColumnIndex(Cols.active_reply));
        String repliesString = getString(getColumnIndex(Cols.replies));
        int color = getInt(getColumnIndex(Cols.color));
        int secondaryColor = getInt(getColumnIndex(Cols.secondary_color));

        ArrayList<UUID> replies = new Gson().fromJson(repliesString, new TypeToken<ArrayList<UUID>>(){}.getType());
        contact.setId(id);
        contact.setName(name);
        contact.setPhone(phone);
        try {
            contact.setActiveReplyId(UUID.fromString(activeReplyId));
        } catch (IllegalArgumentException e) {
            contact.setActiveReplyId(null);
        }
        contact.setReplies(replies);
        contact.setColor(color);
        contact.setSecondaryColor(secondaryColor);
        return contact;
    }
}
