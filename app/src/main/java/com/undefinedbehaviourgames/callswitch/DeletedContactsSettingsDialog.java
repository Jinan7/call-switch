package com.undefinedbehaviourgames.callswitch;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeletedContactsSettingsDialog extends BottomSheetDialogFragment {

    private RadioGroup mRadioGroup;

    public static DeletedContactsSettingsDialog newInstance() {
        DeletedContactsSettingsDialog dialog = new DeletedContactsSettingsDialog();
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.bottom_sheet_deleted_contacts_settings);
        mRadioGroup = (RadioGroup) dialog.findViewById(R.id.settings_deleted_contacts_radio_group);
        initializeRadioGroup();
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup group, int checkedId) {

                if (checkedId == R.id.settings_deleted_contacts_do_nothing) {
                    SettingsPreferences.setDeletedContactSettings(getContext(), DeletedContactSettings.DO_NOTHING);
                } else if (checkedId == R.id.settings_deleted_contacts_highlight) {
                    SettingsPreferences.setDeletedContactSettings(getContext(), DeletedContactSettings.HIGHLIGHT);
                } else if (checkedId == R.id.settings_deleted_contacts_delete){
                    SettingsPreferences.setDeletedContactSettings(getContext(), DeletedContactSettings.DELETE);
                }

                finish(Activity.RESULT_OK);
            }
        });
        return dialog;
    }

    public void initializeRadioGroup() {
        DeletedContactSettings settings = SettingsPreferences.getDeletedContactSettings(getContext());

        switch (settings) {
            case DO_NOTHING:
                mRadioGroup.check(R.id.settings_deleted_contacts_do_nothing);

                break;
            case HIGHLIGHT:
                mRadioGroup.check(R.id.settings_deleted_contacts_highlight);
                break;
            case DELETE:
                mRadioGroup.check(R.id.settings_deleted_contacts_delete);
                break;
        }
    }

    private void finish(int result) {

        getTargetFragment().onActivityResult(getTargetRequestCode(), result, null);
        dismiss();
    }
}
