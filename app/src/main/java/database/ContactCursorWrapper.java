package database;

import android.database.Cursor;

import com.undefinedbehaviourgames.callswitch.Contact;

public class ContactCursorWrapper extends ContactCursorWrapperHelper<Contact>{
    public ContactCursorWrapper(Cursor cursor) {
        super(cursor, Contact.class);
    }
}
