package com.undefinedbehaviourgames.callswitch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class ContactFragment extends Fragment {
    private static final String ARG_ID = "contact_id";
    private TextView mContactIconTextView;
    private TextView mContactNameTextView;
    private Contact mContact;

    public static ContactFragment newInstance(Long id) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Long contactId = getArguments().getLong(ARG_ID);
        mContact = ContactLab.getInstance(getContext()).get(contactId);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        mContactIconTextView = v.findViewById(R.id.contact_contact_icon);
        mContactNameTextView = v.findViewById(R.id.contact_contact_name);
        updateUI();
        return v;
    }

    private void updateUI() {
        mContactNameTextView.setText(mContact.getName());
        mContactIconTextView.setText(mContact.getIcon());
    }
}
