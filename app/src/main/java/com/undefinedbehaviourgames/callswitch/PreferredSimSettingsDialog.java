package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class PreferredSimSettingsDialog extends BottomSheetDialogFragment {

    public static final String TAG = "PreferredSimSettingsDialogLogger";
    private int mMaxSimSlots;
    private RadioGroup mPreferredSimSettingsRadioGroup;
    private SubscriptionManager mSubscriptionManager;
    private ArrayList<RadioButton> mSettingsRadioButtons;

    public static PreferredSimSettingsDialog newInstance() {
        PreferredSimSettingsDialog dialog = new PreferredSimSettingsDialog();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSettingsRadioButtons = new ArrayList<>();
        getSubscriptionInfo();

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.bottom_sheet_preferred_sim);
        mPreferredSimSettingsRadioGroup = (RadioGroup) dialog.findViewById(R.id.preferred_sim_settings_radio_group);

        createSimRadioButtons();
        initializeRadioGroup();

        mPreferredSimSettingsRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {

                //make asynchronous
                if (checkedId == R.id.preferred_sim_use_phone) {
                   SettingsPreferences.setPrefPreferredSimSettings(getContext(), PreferredSimSettings.PHONE_SETTINGS, -1);
                } else if (checkedId == R.id.preferred_sim_receiving_sim) {
                    SettingsPreferences.setPrefPreferredSimSettings(getContext(), PreferredSimSettings.RECEIVING_SIM, -1);
                } else {
                    SettingsPreferences.setPrefPreferredSimSettings(getContext(), PreferredSimSettings.SIM, checkedId);
                }

                finish(Activity.RESULT_OK);
            }
        });
        return dialog;
    }

    public void getSubscriptionInfo() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String [] { Manifest.permission.READ_PHONE_STATE}, PermissionManager.REQUEST_CODE_READ_PHONE_STATE);
            return;
        }
        mSubscriptionManager = getContext().getSystemService(SubscriptionManager.class);
        mMaxSimSlots = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
    }

    public void logSubscriptionInfo() {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        int activeSubscriptionCount = mSubscriptionManager.getActiveSubscriptionInfoCount();
        int maxSimSlots = mSubscriptionManager.getActiveSubscriptionInfoCountMax();
        Log.d(TAG, "Available sims: " + activeSubscriptionCount);
        Log.d(TAG, "Max sim slot: " + maxSimSlots);
    }

    public void createSimRadioButtons() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        for (int i = 0; i < mMaxSimSlots; i++ ) {
            RadioButton settingsButton = (RadioButton) LayoutInflater.from(getContext()).inflate(R.layout.componet_sim_settings_radio_button, null, false);
            String sim = getString(R.string.sim) + " " + (i+1);
            settingsButton.setText(sim);
            //what if the id being set has already been set for one of the radio buttons not created programmatically
            settingsButton.setId(i);
            ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = (int) getResources().getDimension(R.dimen.dp_16);
            params.leftMargin = (int) getResources().getDimension(R.dimen.dp_16);
            params.rightMargin = (int) getResources().getDimension(R.dimen.dp_16);
            settingsButton.setLayoutParams(params);
            settingsButton.setEnabled(false);
            mSettingsRadioButtons.add(settingsButton);
            mPreferredSimSettingsRadioGroup.addView(settingsButton);
        }

        List<SubscriptionInfo> subscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();

        for (SubscriptionInfo info : subscriptionInfoList) {
            mSettingsRadioButtons.get(info.getSimSlotIndex()).setEnabled(true);

        }
        //make read asynchronous, only initialize radio group when read is finished

    }

    public void initializeRadioGroup() {
        SimSettings preferredSimSettings = SettingsPreferences.getPreferredSimSettings(getContext());

        switch (preferredSimSettings.getSettings()) {

            case RECEIVING_SIM:
                mPreferredSimSettingsRadioGroup.check(R.id.preferred_sim_receiving_sim);
                break;
            case SIM:
                int simIndex = preferredSimSettings.getSimIndex();

                if (simIndex >= 0 && simIndex < mSettingsRadioButtons.size()) {
                    mSettingsRadioButtons.get(simIndex).setChecked(true);
                } else {
                    //if sim cannot be found, change to "use phone settings"
                    mPreferredSimSettingsRadioGroup.check(R.id.preferred_sim_use_phone);
                }
                break;
            case PHONE_SETTINGS:
            default:
                mPreferredSimSettingsRadioGroup.check(R.id.preferred_sim_use_phone);
                break;
        }
    }

    public void finish(int resultCode) {

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, null);
        dismiss();
    }

}
