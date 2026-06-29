package com.undefinedbehaviourgames.callswitch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EditReplyFragment extends Fragment {

    private static final String TAG = "EditReplyFragmentLogger";
    private static final int SELECT_CONTACT_REQUEST_CODE = 0;
    private ImageButton mAddContactButton;
    ActivityResultLauncher<Intent> mLauncher;
    private Reply mReply;
    public static EditReplyFragment newInstance() {
        EditReplyFragment fragment = new EditReplyFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReply = new Reply();
        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {


            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() != Activity.RESULT_OK) return;

                Intent data = result.getData();

                if (data != null) {
                    String message = data.getStringExtra("result");
                    Log.d(TAG, message);
                }

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_reply, container, false);
        mAddContactButton = (ImageButton) v.findViewById(R.id.add_contacts_button);
        mAddContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectContactsActivity.newIntent(getContext(), mReply.getId());
                mLauncher.launch(intent);
//                startActivityForResult(intent, SELECT_CONTACT_REQUEST_CODE);
            }
        });
        return v;
    }
}
