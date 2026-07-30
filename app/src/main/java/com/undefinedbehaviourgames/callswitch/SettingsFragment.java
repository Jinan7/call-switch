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

import com.google.android.material.materialswitch.MaterialSwitch;
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
    private MaterialSwitch mSystemSwitch;
    private MaterialSwitch mNotificationSwitch;
    private MaterialSwitch mAllowCallRingSwitch;
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

    private void setUpSystemSwitch() {
       if (mSystemSwitch == null || mSystemSwitchText == null ) return;
       boolean systemIsOn = SettingsPreferences.getSystemSettings(getContext());
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

    private void updateSystemSettings(boolean isChecked) {

        SettingsPreferences.setSystemSettings(getContext(), isChecked);
    }


    private void setUpNotificationSwitch() {
        if (mNotificationSwitch == null) return;
        boolean IsOn = SettingsPreferences.getNotificationSettings(getContext());
        mNotificationSwitch.setChecked(IsOn);
        mNotificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                updateNotificationSettings(isChecked);
            }
        });

    }

    private void updateNotificationSettings(boolean isChecked) {

        SettingsPreferences.setNotificationSettings(getContext(), isChecked);
    }


    private void setUpAllowCallRingSwitch() {
        if (mAllowCallRingSwitch == null) return;

        Boolean IsOn = SettingsPreferences.getAllowCallRingSettings(getContext());
        mAllowCallRingSwitch.setChecked(IsOn);
        mAllowCallRingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                updateAllowCallRingSettings(isChecked);
            }
        });

    }


    private void updateAllowCallRingSettings(boolean isChecked) {

        SettingsPreferences.setAllowCallRingSettings(getContext(), isChecked);
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

    private void updateDeletedContactsUI() {
        DeletedContactSettings settings = SettingsPreferences.getDeletedContactSettings(getContext());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

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
