package database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.undefinedbehaviourgames.callswitch.Contact;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.UUID;

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

        Long id = getLong(getColumnIndex(Schema.Contact.Cols.id));
        String name = getString(getColumnIndex(Schema.Contact.Cols.name));
        String phone = getString(getColumnIndex(Schema.Contact.Cols.phone));
        String activeReplyId = getString(getColumnIndex(Schema.Contact.Cols.active_reply));
        String repliesString = getString(getColumnIndex(Schema.Contact.Cols.replies));

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
        return contact;
    }
}
