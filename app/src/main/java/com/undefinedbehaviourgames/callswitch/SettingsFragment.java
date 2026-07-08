package com.undefinedbehaviourgames.callswitch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    public static final String TAG = "SettingsFragmentLogger";
    private static final String DELETED_CONTACTS_SETTINGS_DIALOG = "deleted contacts settings dialog";
    private static final String PREFERRED_SIM_SETTINGS_DIALOG = "preferred sim settings dialog";
    private static final int DELETED_CONTACTS_SETTINGS_REQUEST_CODE = 0;
    private static final int PREFERRED_SIM_SETTINGS_REQUEST_CODE = 2;
    private LinearLayout mDeletedContactsSettings;
    private LinearLayout mPreferredSimSettings;
    private TextView mPreferredSimTextView;
    private TextView mDeletedContactsTextView;
    private PreferredSimSettingsDialog mPreferredSimSettingsDialog;
    private SwitchMaterial mSystemSwitch;
    private SwitchMaterial mNotificationSwitch;
    private SwitchMaterial mAllowCallRingSwitch;
    private TextView mSystemSwitchText;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferredSimSettingsDialog = PreferredSimSettingsDialog.newInstance();
        mPreferredSimSettingsDialog.setTargetFragment(SettingsFragment.this, PREFERRED_SIM_SETTINGS_REQUEST_CODE);


    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        mDeletedContactsSettings = v.findViewById(R.id.settings_deleted_contacts);
        mDeletedContactsSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeletedContactsSettingsDialog dialog = DeletedContactsSettingsDialog.newInstance();
                dialog.setTargetFragment(SettingsFragment.this, DELETED_CONTACTS_SETTINGS_REQUEST_CODE);
                dialog.show(getParentFragmentManager(), DELETED_CONTACTS_SETTINGS_DIALOG);
            }
        });

        mPreferredSimSettings = v.findViewById(R.id.settings_preferred_sim);
        mPreferredSimSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferredSimSettingsDialog.show(getParentFragmentManager(), PREFERRED_SIM_SETTINGS_DIALOG);
            }
        });
        mPreferredSimTextView = v.findViewById(R.id.settings_preferred_sim_text);
        mDeletedContactsTextView = v.findViewById(R.id.settings_deleted_contacts_text);
        mSystemSwitch = v.findViewById(R.id.settings_system_switch);
        mNotificationSwitch = v.findViewById(R.id.settings_reply_notification_switch);
        mAllowCallRingSwitch = v.findViewById(R.id.settings_let_call_ring_switch);
        mSystemSwitchText = (TextView) v.findViewById(R.id.settings_system_switch_text);
        updatePreferredSimUI();
        updateDeletedContactsUI();
        setUpSystemSwitch();
        setUpNotificationSwitch();
        setUpAllowCallRingSwitch();
        return v;
    }

    private int getColor(int id) {
        return ResourcesCompat.getColor(getResources(), id, getActivity().getTheme());
    }
    @SuppressLint("StaticFieldLeak")
    private void setUpSystemSwitch() {
       if (mSystemSwitch == null || mSystemSwitchText == null ) return;

       new AsyncTask<Void, Void, Boolean>() {
           @Override
           protected Boolean doInBackground(Void... voids) {
               Boolean settings = SettingsPreferences.getSystemSettings(getContext());
               return settings;
           }

           @Override
           protected void onPostExecute(Boolean systemIsOn) {
               super.onPostExecute(systemIsOn);

               if (systemIsOn) {
                   mSystemSwitchText.setTextColor(getColor(R.color.blue));
                   mSystemSwitchText.setText(R.string.on);
               } else {
                   mSystemSwitchText.setTextColor(getColor(R.color.grey_10));
                   mSystemSwitchText.setText(R.string.off);

               }
               mSystemSwitch.setChecked(systemIsOn);
               mSystemSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                   @Override
                   public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                       if (isChecked) {
                           mSystemSwitchText.setTextColor(getColor(R.color.blue));
                           mSystemSwitchText.setText(R.string.on);
                       } else {
                           mSystemSwitchText.setTextColor(getColor(R.color.grey_10));
                           mSystemSwitchText.setText(R.string.off);
                       }
                       updateSystemSettings(isChecked);
                   }
               });
           }
       }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateSystemSettings(boolean isChecked) {
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... voids) {
                SettingsPreferences.setSystemSettings(getContext(), isChecked);
                return null;
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void setUpNotificationSwitch() {
        if (mNotificationSwitch == null) return;

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Boolean settings = SettingsPreferences.getNotificationSettings(getContext());
                return settings;
            }

            @Override
            protected void onPostExecute(Boolean IsOn) {
                super.onPostExecute(IsOn);

                mNotificationSwitch.setChecked(IsOn);
                mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                        updateNotificationSettings(isChecked);
                    }
                });
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateNotificationSettings(boolean isChecked) {
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... voids) {
                SettingsPreferences.setNotificationSettings(getContext(), isChecked);
                return null;
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void setUpAllowCallRingSwitch() {
        if (mAllowCallRingSwitch == null) return;

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                Boolean settings = SettingsPreferences.getAllowCallRingSettings(getContext());
                return settings;
            }

            @Override
            protected void onPostExecute(Boolean IsOn) {
                super.onPostExecute(IsOn);

                mAllowCallRingSwitch.setChecked(IsOn);
                mAllowCallRingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                        updateAllowCallRingSettings(isChecked);
                    }
                });
            }
        }.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private void updateAllowCallRingSettings(boolean isChecked) {
        new AsyncTask<Void, Void, Void> () {
            @Override
            protected Void doInBackground(Void... voids) {
                SettingsPreferences.setAllowCallRingSettings(getContext(), isChecked);
                return null;
            }
        }.execute();
    }



    private void updatePreferredSimUI() {
        //make asynchronous
        SimSettings preferredSimSettings = SettingsPreferences.getPreferredSimSettings(getContext());

        switch (preferredSimSettings.getSettings()) {

            case RECEIVING_SIM:
                mPreferredSimTextView.setText(R.string.sim_receiving_call);
                break;
            case SIM:

                if (preferredSimSettings.getSimIndex() >= 0) {
                    String sim = getString(R.string.sim) + " " + (preferredSimSettings.getSimIndex() + 1);
                    mPreferredSimTextView.setText(sim);
                } else {
                    mPreferredSimTextView.setText(R.string.use_phone_settings);
                }
                break;
            case PHONE_SETTINGS:
            default:
                mPreferredSimTextView.setText(R.string.use_phone_settings);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void updateDeletedContactsUI() {
        DeletedContactSettings settings = SettingsPreferences.getDeletedContactSettings(getContext());

        new AsyncTask<Void, Void, DeletedContactSettings> () {
            @Override
            protected DeletedContactSettings doInBackground(Void... voids) {
                DeletedContactSettings settings = SettingsPreferences.getDeletedContactSettings(getContext());
                return settings;
            }

            @Override
            protected void onPostExecute(DeletedContactSettings settings) {
                super.onPostExecute(settings);
                switch (settings) {
                    case DO_NOTHING:
                        mDeletedContactsTextView.setText(R.string.do_nothing);
                        break;
                    case HIGHLIGHT:
                        mDeletedContactsTextView.setText(R.string.highlight);
                        break;
                    case DELETE:
                        mDeletedContactsTextView.setText(R.string.delete_in_app);
                        break;
                }
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PermissionManager.REQUEST_CODE_READ_PHONE_STATE:
                if (mPreferredSimSettingsDialog != null) mPreferredSimSettingsDialog.logSubscriptionInfo();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case PREFERRED_SIM_SETTINGS_REQUEST_CODE:
                updatePreferredSimUI();
                break;
            case DELETED_CONTACTS_SETTINGS_REQUEST_CODE:
                updateDeletedContactsUI();
                break;
        }
    }


}
