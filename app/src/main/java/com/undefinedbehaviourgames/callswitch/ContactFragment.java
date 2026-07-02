package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.Priority.HIGH;
import static com.undefinedbehaviourgames.callswitch.Priority.LOW;
import static com.undefinedbehaviourgames.callswitch.Priority.NORMAL;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ContactFragment extends Fragment {
    private static final String ARG_ID = "contact_id";
    private TextView mContactIconTextView;
    private TextView mContactNameTextView;
    private TextView mContactPhoneTextView;
    private TextView mContactActiveReplyTextView;
    private FrameLayout mPriorityIcon;
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
        mContactPhoneTextView = v.findViewById(R.id.contact_contact_phone);
        mContactActiveReplyTextView = v.findViewById(R.id.contact_active_reply);
        mPriorityIcon = v.findViewById(R.id.contact_reply_priority_button);
        updateUI();
        return v;
    }

    private void updateUI() {
        mContactNameTextView.setText(mContact.getName());
        mContactIconTextView.setText(mContact.getIcon());
        mContactPhoneTextView.setText(mContact.getPhone());
        mContactActiveReplyTextView.setText(mContact.getActiveReplyText(getContext()));

        GradientDrawable background = (GradientDrawable) mPriorityIcon.getBackground();
        background.mutate();
        int stroke_width = (int)getResources().getDimension(R.dimen.circle_stroke_2);
        switch (mContact.getActiveReplyPriority(getContext())) {

            case LOW:
                background.setColor(getResources().getColor(R.color.priority_green_2, getContext().getTheme()));
                background.setStroke(stroke_width, getResources().getColor(R.color.priority_green_3, getContext().getTheme()));
                break;
            case NORMAL:
                background.setColor(getResources().getColor(R.color.priority_blue_1, getContext().getTheme()));
                background.setStroke(stroke_width, getResources().getColor(R.color.priority_blue_2, getContext().getTheme()));
                break;
            case HIGH:
                background.setColor(getResources().getColor(R.color.priority_red_1, getContext().getTheme()));
                background.setStroke(stroke_width, getResources().getColor(R.color.priority_red_2, getContext().getTheme()));
                break;
            default:
                break;

        }
    }
}
