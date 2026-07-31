package com.undefinedbehaviourgames.callswitch;

import static android.content.Context.TELEPHONY_SERVICE;

import android.Manifest;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;


import androidx.core.app.ActivityCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.lang.ref.WeakReference;
import java.util.Random;

enum State { FETCHED, FETCHING, IDLE}
public class ContactQueryHandler extends AsyncQueryHandler {
    private static final String TAG = "ContactQueryHandlerLogger";
    private static ContactQueryHandler sContactQueryHandler;
    private final Context mContext;
    public static final int TOKEN_CONTACT = 0;
    public static final int TOKEN_PHONE = 1;
    public static final int TOKEN_LAST_PHONE = 3;
    public final int DISPLAY_NAME_INDEX = 1;
    public final int PHONE_INDEX = 1;
    public final int CONTACT_ID_INDEX = 0;
    public final int LOOKUP_KEY_INDEX = 2;
    private final int [] colors;
    private final int [] colorsSecondary;
    private WeakReference<Callbacks> mCallbacks;
    private State queryState = State.IDLE;
    private TelephonyManager mTelephonyManager;
    private final String[] CONTACT_PROJECTION = new String [] {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.LOOKUP_KEY,
    };
    protected final String[] PHONE_PROJECTION = new String [] {
            ContactsContract.CommonDataKinds.Phone._ID,
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };


    public ContactQueryHandler(Context context) {
        super(context.getContentResolver());
        mCallbacks = new WeakReference<>(null);
        colors = context.getResources().getIntArray(R.array.contact_colors);
        colorsSecondary = context.getResources().getIntArray(R.array.contact_colors_dark);
        mContext = context.getApplicationContext();
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(TELEPHONY_SERVICE);
    }

    public static ContactQueryHandler getInstance(Context context) {
        if (sContactQueryHandler == null) {
            sContactQueryHandler = new ContactQueryHandler(context);
        }
        return sContactQueryHandler;
    }

    public State getQueryState() {
        return queryState;
    }

    public void setQueryState(State queryState) {
        this.queryState = queryState;
    }

    public void startQuery(WeakReference<Callbacks> callbacks) {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (callbacks != null) mCallbacks = callbacks;

        if (queryState != State.IDLE) return;


        queryState = State.FETCHING;

        startQuery(TOKEN_CONTACT,
                null,
                ContactsContract.Contacts.CONTENT_URI,
                CONTACT_PROJECTION,
                ContactsContract.Contacts.HAS_PHONE_NUMBER,
                null,
                ContactsContract.Contacts.DISPLAY_NAME + " ASC");



    }
    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);

        switch (token) {

            case TOKEN_CONTACT:
                onContactQueryComplete(cursor);
                break;

            case TOKEN_PHONE:
                onPhoneQueryComplete(cursor, (Contact) cookie, false);
                break;
            case TOKEN_LAST_PHONE:
                onPhoneQueryComplete(cursor, (Contact) cookie, true);
                break;
            default:
                break;
        }
    }

    public void onContactQueryComplete(Cursor cursor) {
        try {
            if (cursor.getCount() == 0) return;
            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {
                String name = cursor.getString(DISPLAY_NAME_INDEX);
                Long id = cursor.getLong(CONTACT_ID_INDEX);
                String lookupkey = cursor.getString(LOOKUP_KEY_INDEX);
                Contact contact = new Contact();

                Random random = new Random();
                int colorIndex = random.nextInt(colors.length);


                contact.setName(name);
                contact.setId(id);
                contact.setLookupKey(lookupkey);
                contact.setColor(colors[colorIndex]);
                contact.setSecondaryColor(colorsSecondary[colorIndex]);
                Uri.Builder builder = ContactsContract.Contacts.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, cursor.getLong(CONTACT_ID_INDEX));
                builder.appendEncodedPath(ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
                Uri phoneNumbersUri = builder.build();

                int token = cursor.isLast() ? TOKEN_LAST_PHONE : TOKEN_PHONE;
                startQuery(token,
                        contact,
                        phoneNumbersUri,
                        PHONE_PROJECTION,
                        ContactsContract.CommonDataKinds.Phone.MIMETYPE + "=?",
                        new String[] {ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE},
                        null);

                cursor.moveToNext();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public void onPhoneQueryComplete(Cursor cursor, Contact contact, boolean last) {
        try {
            if (cursor.getCount() == 0) return;
            cursor.moveToFirst();
            String phone = cursor.getString(PHONE_INDEX);
            contact.setPhone(phone);
            String country = mTelephonyManager.getNetworkCountryIso();
            PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                PhoneNumber number = phoneNumberUtil.parse(phone, country.toUpperCase());
                contact.setPhoneNumber(number);
                Log.d(TAG, number.toString());
            } catch (NumberParseException e) {
                e.printStackTrace();
            }
            ContactLab.getInstance(mContext).addContactToPhoneImage(contact);
            ContactLab.getInstance(mContext).add(contact);
            Callbacks callbacks = mCallbacks.get();

            if (last) {
                DeletedContactSettings settings = SettingsPreferences.getDeletedContactSettings(mContext);
                //make asynchronous
                //deleted all contacts that have been deleted from phone book if settings say so
                if (settings == DeletedContactSettings.DELETE) ContactLab.getInstance(mContext).removeDeleted();

                synchronized (this) {
                    queryState = State.FETCHED;

                    if (callbacks != null) {

                        callbacks.onQueryComplete();
                    }
                }

            }


        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public WeakReference<Callbacks> getCallbacks() {
        return mCallbacks;
    }

    public void setCallbacks(WeakReference<Callbacks> callbacks) {
        mCallbacks = callbacks;
    }

    public interface Callbacks {
        void onQueryComplete();

    }

}
