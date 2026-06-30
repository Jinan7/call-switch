package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.EDIT_REPLY;
import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.NEW_REPLY;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditReplyFragment extends Fragment {

    private static final String TAG = "EditReplyFragmentLogger";
    private static final String ARGS_ID = "reply_id";
    private static final String ARGS_MODE = "mode";
    private static final int SELECT_CONTACT_REQUEST_CODE = 0;
    public static final String EXTRA_SELECTED_CONTACTS = "com.undefinedbehaviourgames.callswitch.selected_contacts";
    private int mode;
    private ImageButton mAddContactButton;
    private RecyclerView mRecyclerView;
    private MaterialToolbar mToolbar;
    private TextInputEditText mReplyTextField;
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
            mReply = ReplyLab.getInstance().get(id);
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
                    ArrayList<Contact> selectedContacts = (ArrayList<Contact>) data.getSerializableExtra(EXTRA_SELECTED_CONTACTS);
                    mReply.setReplyToList(selectedContacts);
                    ContactAdapter adapter = (ContactAdapter) mRecyclerView.getAdapter();
                    adapter.setContacts(mReply.getReplyToList());
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
        }

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.menu_save_reply) {

                    switch (mode) {
                        case NEW_REPLY:
                            ReplyLab.getInstance().add(mReply);
                        case EDIT_REPLY:
                            ReplyLab.getInstance().update(mReply);
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
                Intent intent = SelectContactsActivity.newIntent(getContext(), mReply.getId());
                mLauncher.launch(intent);
            }
        });
        mRecyclerView = v.findViewById(R.id.selected_contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new ContactAdapter(mReply.getReplyToList()));
        mReplyTextField = v.findViewById(R.id.reply_text_field);
        mReplyTextField.setText(mReply.getReply());
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
        return v;
    }

    private class ContactHolder extends RecyclerView.ViewHolder {

        private Contact mContact;
        private TextView mContactName;
        private TextView mContactPhone;
        private TextView mContactIcon;
        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            mContactName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactPhone = (TextView) itemView.findViewById(R.id.contact_phone);
            mContactIcon = (TextView) itemView.findViewById(R.id.contact_icon);
        }

        public void bind(Contact contact) {
            mContact = contact;
            mContactName.setText(contact.getName());
            mContactPhone.setText(contact.getPhone());
            mContactIcon.setText(contact.getIcon());
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
            View v= LayoutInflater.from(getContext()).inflate(R.layout.component_contact, parent, false);
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
