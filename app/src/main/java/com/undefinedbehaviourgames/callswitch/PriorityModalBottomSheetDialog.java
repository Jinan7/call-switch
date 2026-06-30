package com.undefinedbehaviourgames.callswitch;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PriorityModalBottomSheetDialog extends BottomSheetDialogFragment {

    public static PriorityModalBottomSheetDialog newInstance() {
        PriorityModalBottomSheetDialog dialog = new PriorityModalBottomSheetDialog();
        return dialog;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.bottom_sheet_priority);
        return dialog;
    }
}
