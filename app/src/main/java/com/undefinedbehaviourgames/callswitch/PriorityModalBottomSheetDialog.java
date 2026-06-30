package com.undefinedbehaviourgames.callswitch;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PriorityModalBottomSheetDialog extends BottomSheetDialogFragment {

    public static String EXTRA_PRIORITY = "com.undefinedbehaviourgames.callswitch.priority";
    private Priority mPriority;
    private TextView mPriorityHigh;
    private TextView mPriorityNormal;
    private TextView mPriorityLow;
    public static PriorityModalBottomSheetDialog newInstance() {
        PriorityModalBottomSheetDialog dialog = new PriorityModalBottomSheetDialog();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPriority = Priority.NORMAL;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        dialog.setContentView(R.layout.bottom_sheet_priority);
        mPriorityHigh = (TextView) dialog.findViewById(R.id.priority_high);
        mPriorityHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority  = Priority.HIGH;
                finish(Activity.RESULT_OK);
            }
        });
        mPriorityNormal = (TextView) dialog.findViewById(R.id.priority_normal);
        mPriorityNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority = Priority.NORMAL;
                finish(Activity.RESULT_OK);
            }
        });
        mPriorityLow = (TextView) dialog.findViewById(R.id.priority_low);
        mPriorityLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPriority = Priority.LOW;
                finish(Activity.RESULT_OK);
            }
        });
        return dialog;
    }

    public void finish(int resultCode) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_PRIORITY, mPriority);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
        dismiss();
    }
}
