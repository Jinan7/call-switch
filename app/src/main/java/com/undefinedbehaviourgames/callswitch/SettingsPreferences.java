package com.undefinedbehaviourgames.callswitch;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

enum DeletedContactSettings {
    DO_NOTHING, HIGHLIGHT, DELETE
}

enum PreferredSimSettings {
    PHONE_SETTINGS, RECEIVING_SIM, SIM
}


public class SettingsPreferences {

    public static final String  PREF_DELETED_CONTACTS_SETTINGS = "deleted contacts settings";
    public static final String  PREF_NOTIFICATION_SETTINGS = "notification settings";
    public static final String  PREF_ALLOW_CALL_RING_SETTINGS = "allow call ring settings";
    public static final String PREF_PREFERRED_SIM_SETTINGS = "preferred sim settings";
    private static final String defaultPreferredSimSettingsString = new Gson().toJson(new SimSettings(PreferredSimSettings.PHONE_SETTINGS, -1));
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

    public static SimSettings getPreferredSimSettings(Context context) {

        String settingsString = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_PREFERRED_SIM_SETTINGS, defaultPreferredSimSettingsString);

        return new Gson().fromJson(settingsString, new TypeToken<SimSettings>() {}.getType());
    }

    public static  void setPrefPreferredSimSettings(Context context, PreferredSimSettings settings, int simIdx) {
        SimSettings simSettings = new SimSettings(settings, simIdx);
        String settingsString = new Gson().toJson(simSettings);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_PREFERRED_SIM_SETTINGS, settingsString)
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
