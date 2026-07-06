package com.undefinedbehaviourgames.callswitch;

import android.content.Context;
import android.preference.PreferenceManager;

enum DeletedContactSettings {
    DO_NOTHING, HIGHTLIGHT, DELETE
}
public class SettingsPreferences {

    public static final String  PREF_DELETED_CONTACTS_SETTINGS = "deleted contacts settings";
    public static final String  PREF_NOTIFICATION_SETTINGS = "notification settings";
    public static final String  PREF_ALLOW_CALL_RING_SETTINGS = "notification settings";
    public static DeletedContactSettings getDeletedContactSettings(Context context) {
        int defaultValue = DeletedContactSettings.DO_NOTHING.ordinal();
        int settingsOrdinal = PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_DELETED_CONTACTS_SETTINGS, defaultValue);

        return DeletedContactSettings.values()[settingsOrdinal];
    }

    public static void setDeletedContactSettings(Context context, DeletedContactSettings settings) {

        int settingsOrdinal = settings.ordinal();
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_DELETED_CONTACTS_SETTINGS, settingsOrdinal)
                .apply();
    }
    public static boolean getNotificationSettings(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_NOTIFICATION_SETTINGS, true);
    }

    public static void setNotificationSettings(Context context, boolean settings) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_NOTIFICATION_SETTINGS, settings)
                .apply();
    }

    public static boolean getAllowCallRingSettings(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_ALLOW_CALL_RING_SETTINGS, false);
    }

    public static void setAllowCallRingSettings(Context context, boolean settings) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_ALLOW_CALL_RING_SETTINGS, settings)
                .apply();
    }
}
