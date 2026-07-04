package com.undefinedbehaviourgames.callswitch;

import static android.app.Activity.RESULT_OK;
import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.EDIT_REPLY;
import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.NEW_REPLY;
import static com.undefinedbehaviourgames.callswitch.PriorityModalBottomSheetDialog.EXTRA_PRIORITY;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditReplyFragment extends Fragment {

    private static final String TAG = "EditReplyFragmentLogger";
    private static final String PRIORITY_DIALOG_TAG = "Priority dialog";
    private static final int PRIORITY_REQUEST_CODE = 0;
    private static final String ARGS_ID = "reply_id";
    private static final String ARGS_MODE = "mode";
    private static final int SELECT_CONTACT_REQUEST_CODE = 0;
    public static final String EXTRA_SELECTED_CONTACTS = "com.undefinedbehaviourgames.callswitch.selected_contacts";
    private int mode;
    private ImageButton mAddContactButton;
    private RecyclerView mRecyclerView;
    private MaterialToolbar mToolbar;
    private TextInputEditText mReplyTextField;
    private LinearLayout mPriorityButton;
    private TextView mPriorityTextView;
    private SwitchMaterial mEnableSwitch;
    private SwitchMaterial mReplyUnknownSwitch;
    private SwitchMaterial mReplaceEqualPrioritySwitch;
    ActivityResultLauncher<Intent> mLauncher;
    private Reply mReply;
    public static EditReplyFragment newInstance(int mode) {
        EditReplyFragment fragment = new EditReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_MODE, mode);
        fragment.setArguments(args);
        return fragment;
    }

    public static EditReplyFragment newInstance(int mode, UUID id) {
        EditReplyFragment fragment = new EditReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_MODE, mode);
        args.putSerializable(ARGS_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getArguments().getInt(ARGS_MODE);

        if (mode == EDIT_REPLY) {

            UUID id = (UUID) getArguments().getSerializable(ARGS_ID);
            mReply = ReplyLab.getInstance(getContext()).get(id);
        } else {
            mReply = new Reply();
        }


        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {


            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() != Activity.RESULT_OK) return;

                Intent data = result.getData();

                if (data != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Long> selectedContacts = (ArrayList<Long>) data.getSerializableExtra(EXTRA_SELECTED_CONTACTS);
                    mReply.setReplyToList(selectedContacts);
                    ContactAdapter adapter = (ContactAdapter) mRecyclerView.getAdapter();
                    adapter.setContacts(mReply.getReplyToList(getContext()));
                    adapter.notifyDataSetChanged();
                }

            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_reply, container, false);
        mToolbar = (MaterialToolbar) v.findViewById(R.id.edit_reply_toolbar);

        if (mode == EDIT_REPLY) {
            mToolbar.inflateMenu(R.menu.edit_reply_menu);
        } else {
            mToolbar.inflateMenu(R.menu.new_reply_menu);
        }

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.menu_save_reply) {

                    switch (mode) {
                        case NEW_REPLY:
                            ReplyLab.getInstance(getContext()).add(getContext(), mReply);
                        case EDIT_REPLY:
                            ReplyLab.getInstance(getContext()).update(getContext(), mReply);

                    }

                    getActivity().finish();
                    return true;
                }
                return false;
            }
        });
        mAddContactButton = (ImageButton) v.findViewById(R.id.add_contacts_button);
        mAddContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = SelectContactsActivity.newIntent(getContext(), mReply.getId(), mReply.getReplyToList(getContext()));
                mLauncher.launch(intent);
            }
        });
        mRecyclerView = v.findViewById(R.id.selected_contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new ContactAdapter(mReply.getReplyToList(getContext())));
        mReplyTextField = v.findViewById(R.id.reply_text_field);
        mReplyTextField.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mReply.setReply(s.toString());
            }
        });
        mPriorityButton = (LinearLayout) v.findViewById(R.id.priority_button);
        mPriorityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PriorityModalBottomSheetDialog dialog = PriorityModalBottomSheetDialog.newInstance();
                dialog.setTargetFragment(EditReplyFragment.this, PRIORITY_REQUEST_CODE);
                dialog.show(getParentFragmentManager(), PRIORITY_DIALOG_TAG);
            }
        });
        mPriorityTextView = v.findViewById(R.id.priority);
        mEnableSwitch = v.findViewById(R.id.enable_reply_switch);

        mReplyUnknownSwitch = v.findViewById(R.id.reply_unknown_switch);
        mReplaceEqualPrioritySwitch = v.findViewById(R.id.replace_same_priority_switch);

        mEnableSwitch.setChecked(mReply.isEnabled());
        mEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                mReply.setEnabled(isChecked);
            }
        });

        mReplyUnknownSwitch.setChecked(mReply.replyUnknown());
        mReplyUnknownSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                mReply.setReplyUnknown(isChecked);
            }
        });

        mReplaceEqualPrioritySwitch.setChecked(mReply.replaceEqualPriority());
        mReplaceEqualPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                mReply.setReplaceEqualPriority(isChecked);
            }
        });
        updateUI();
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PRIORITY_REQUEST_CODE:
                if (data != null) {
                    Priority priority = (Priority) data.getSerializableExtra(EXTRA_PRIORITY);
                    mReply.setPriority(priority);
                    updateUI();
                }

        }
    }

    private void updateUI() {
        mReplyTextField.setText(mReply.getReply());
        mPriorityTextView.setText(mReply.getPriorityText(getContext()));
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Contact mContact;
        private TextView mContactName;
        private TextView mContactPhone;
        private TextView mContactIcon;
        private ImageButton mRemoveContactButton;
        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            mContactName = (TextView) itemView.findViewById(R.id.reply_contact_name);
            mContactPhone = (TextView) itemView.findViewById(R.id.reply_contact_phone);
            mContactIcon = (TextView) itemView.findViewById(R.id.reply_contact_icon);
            mRemoveContactButton = (ImageButton) itemView.findViewById(R.id.reply_contact_remove_button);

        }

        public void bind(Contact contact) {
            mContact = contact;
            mContactName.setText(contact.getName());
            mContactPhone.setText(contact.getPhone());
            mContactIcon.setText(contact.getIcon());

            GradientDrawable background = (GradientDrawable) mContactIcon.getBackground();
            background.mutate();
            background.setColor(contact.getColor());
            mContactIcon.setTextColor(contact.getSecondaryColor());
            mRemoveContactButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mReply.deleteContact(mContact);
            ((ContactAdapter) mRecyclerView.getAdapter()).setContacts(mReply.getReplyToList(getContext()));
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private class ContactAdapter extends RecyclerView.Adapter<ContactHolder> {

        private List<Contact> mContacts;
        public ContactAdapter(List<Contact> contacts) {
            mContacts = contacts;
        }
        @NonNull
        @Override
        public ContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v= LayoutInflater.from(getContext()).inflate(R.layout.component_reply_contact, parent, false);
            return new ContactHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactHolder holder, int position) {
            holder.bind(mContacts.get(position));
        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }

        public void setContacts(List<Contact> contacts) {
            mContacts = contacts;
        }
    }
}
