package com.undefinedbehaviourgames.callswitch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private static final String DELETED_CONTACTS_SETTINGS_DIALOG = "deleted contacts settings dialog";
    private static final String PREFERRED_SIM_SETTINGS_DIALOG = "preferred sim settings dialog";
    private static final int DELETED_CONTACTS_SETTINGS_REQUEST_CODE = 0;
    private static final int PREFERRED_SIM_SETTINGS_REQUEST_CODE = 2;
    LinearLayout mDeletedContactsSettings;
    LinearLayout mPreferredSimSettings;
    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
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
                PreferredSimSettingsDialog dialog = PreferredSimSettingsDialog.newInstance();
                dialog.setTargetFragment(SettingsFragment.this, PREFERRED_SIM_SETTINGS_REQUEST_CODE);
                dialog.show(getParentFragmentManager(), PREFERRED_SIM_SETTINGS_DIALOG);
            }
        });
        return v;
    }
}
