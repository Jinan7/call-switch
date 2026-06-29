package com.undefinedbehaviourgames.callswitch;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class EditReplyFragment extends Fragment {

    private ImageButton mAddContactButton;
    public static EditReplyFragment newInstance() {
        EditReplyFragment fragment = new EditReplyFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_reply, container, false);
        mAddContactButton = (ImageButton) v.findViewById(R.id.add_contacts_button);
        mAddContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectContactsActivity.newIntent(getContext());
                startActivity(intent);
            }
        });
        return v;
    }
}
