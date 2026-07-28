package com.undefinedbehaviourgames.callswitch;

import android.content.Context;
import android.preference.PreferenceManager;

import androidx.core.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ContactPreferences {

    public static final String  PREF_UNKNOWN_CONTACT = "unknown contact";

    public static Contact getUnknownContact(Context context) {
        Contact defaultUnknownContact = new Contact();
        defaultUnknownContact.setPhone(context.getString(R.string.unknown_contacts_phone_label));
        defaultUnknownContact.setName(context.getString(R.string.unknown_contacts_label));
        defaultUnknownContact.setColor(getColor(context, R.color.grey_7));
        defaultUnknownContact.setSecondaryColor(getColor(context, R.color.grey_1));
        String contactString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_UNKNOWN_CONTACT, new Gson().toJson(defaultUnknownContact));

        return new Gson().fromJson(contactString, new TypeToken<Contact>(){}.getType());
    }


    public static void setUnknownContact(Context context, Contact contact) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_UNKNOWN_CONTACT, new Gson().toJson(contact))
                .apply();
    }

    private static int getColor(Context context, int rId) {
        return ResourcesCompat.getColor(context.getResources(), rId, context.getTheme());
    }
}
