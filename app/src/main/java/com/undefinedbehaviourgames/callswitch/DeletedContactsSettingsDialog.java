package com.undefinedbehaviourgames.callswitch;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeletedContactsSettingsDialog extends BottomSheetDialogFragment {

    public static DeletedContactsSettingsDialog newInstance() {
        DeletedContactsSettingsDialog dialog = new DeletedContactsSettingsDialog();
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.bottom_sheet_deleted_contacts_settings);
        return dialog;
    }
}
