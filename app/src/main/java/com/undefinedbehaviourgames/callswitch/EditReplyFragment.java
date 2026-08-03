package com.undefinedbehaviourgames.callswitch;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.ROLE_SERVICE;
import static android.view.View.GONE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.EDIT_REPLY;
import static com.undefinedbehaviourgames.callswitch.EditReplyActivity.NEW_REPLY;
import static com.undefinedbehaviourgames.callswitch.PriorityModalBottomSheetDialog.EXTRA_PRIORITY;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_ADDED;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_DELETED;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_INDEX;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_UPDATED;

import android.animation.Animator;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditReplyFragment extends Fragment implements  Reply.Callbacks {

    private static final String TAG = "EditReplyFragmentLogger";
    private static final String PRIORITY_DIALOG_TAG = "Priority dialog";
    private static final int PRIORITY_REQUEST_CODE = 0;
    private static final String ARGS_ID = "reply_id";
    private static final String ARGS_MODE = "mode";
    private static final String ARGS_REPLY_IDX = "reply_index";
    private static final int SELECT_CONTACT_REQUEST_CODE = 0;
    public static final String EXTRA_SELECTED_CONTACTS = "com.undefinedbehaviourgames.callswitch.selected_contacts";
    public static final String STATE_PRIORITY = "priority";
    public static final String STATE_REPLY_TO_LIST = "replyToList";
    private int mode;
    private int replyIndex;
    private Priority mReplyPriority;
    private ImageButton mAddContactButton;
    private RecyclerView mRecyclerView;
    private MaterialToolbar mToolbar;
    private TextInputEditText mReplyTextField;
    private TextInputLayout mReplyTextInputLayout;
    private LinearLayout mPriorityButton;
    private TextView mPriorityTextView;
    private MaterialSwitch mEnableSwitch;
    private MaterialSwitch mReplyUnknownSwitch;
    private MaterialSwitch mReplaceEqualPrioritySwitch;
    private ExecutorService mExecutorService;
    private boolean fetch_complete = false;
    private List<Contact> mContacts;
    ActivityResultLauncher<Intent> mLauncher;
    private Reply mReply;

    public static EditReplyFragment newInstance(int mode, int replyIdx) {
        EditReplyFragment fragment = new EditReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_MODE, mode);
        args.putInt(ARGS_REPLY_IDX, replyIdx);
        fragment.setArguments(args);
        return fragment;
    }

    public static EditReplyFragment newInstance(int mode, UUID id, int replyIdx) {
        EditReplyFragment fragment = new EditReplyFragment();
        Bundle args = new Bundle();
        args.putInt(ARGS_MODE, mode);
        args.putSerializable(ARGS_ID, id);
        args.putInt(ARGS_REPLY_IDX, replyIdx);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mode = getArguments().getInt(ARGS_MODE);
        replyIndex = getArguments().getInt(ARGS_REPLY_IDX);
        if (mode == EDIT_REPLY) {

            UUID id = (UUID) getArguments().getSerializable(ARGS_ID);
            mReply = ReplyLab.getInstance(getContext()).get(id);
        } else {
            mReply = new Reply();
        }

        if (savedInstanceState != null) {
            mReplyPriority = (Priority) savedInstanceState.getSerializable(STATE_PRIORITY);
            if (mReplyPriority != null) mReply.setPriority(mReplyPriority);
            ArrayList<String> outReplyToList = (ArrayList<String>) savedInstanceState.getSerializable(STATE_REPLY_TO_LIST);
            if (outReplyToList != null) mReply.setReplyToList(outReplyToList);
        }


        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {


            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() != Activity.RESULT_OK) return;

                Intent data = result.getData();

                if (data != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<String> selectedContacts = (ArrayList<String>) data.getSerializableExtra(EXTRA_SELECTED_CONTACTS);
                    mReply.setReplyToList(selectedContacts);
                    ContactAdapter adapter = (ContactAdapter) mRecyclerView.getAdapter();
                    adapter.setContacts(mReply.getReplyToList(getContext()));
                    adapter.notifyDataSetChanged();
                }

            }
        });

        mExecutorService = Executors.newSingleThreadExecutor();
        getReplyToListAsync();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        String replyToJsonString = new Gson().toJson(mReply.getReplyToList());
        outState.putSerializable(STATE_PRIORITY, mReplyPriority);
        outState.putSerializable(STATE_REPLY_TO_LIST, (ArrayList<String>)mReply.getReplyToList());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
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

                    if (mode == NEW_REPLY) {

                        Intent newReplyIntent = new Intent();
                        newReplyIntent.putExtra(EXTRA_REPLY_ADDED, mReply.getId());
                        getActivity().setResult(RESULT_OK, newReplyIntent);
                        mExecutorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                ReplyLab.getInstance(getContext()).add(getContext(), mReply);
                            }
                        });
                    } else if (mode == EDIT_REPLY) {

                        Intent editReplyIntent = new Intent();
                        editReplyIntent.putExtra(EXTRA_REPLY_UPDATED, mReply.getId());
                        editReplyIntent.putExtra(EXTRA_REPLY_INDEX, replyIndex);
                        getActivity().setResult(RESULT_OK, editReplyIntent);

                        mExecutorService.execute(new Runnable() {

                            @Override
                            public void run() {
                                ReplyLab.getInstance(getContext()).update(getContext(), mReply);
                            }
                        });
                    }

                    getActivity().finish();
                    return true;
                } else if (item.getItemId() == R.id.menu_delete_reply) {
                    Intent deleteReplyIntent = new Intent();
                    deleteReplyIntent.putExtra(EXTRA_REPLY_DELETED, mReply.getId());
                    deleteReplyIntent.putExtra(EXTRA_REPLY_INDEX, replyIndex);
                    getActivity().setResult(RESULT_OK, deleteReplyIntent);
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            ReplyLab.getInstance(getContext()).delete(getContext(), mReply);
                        }
                    });
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
        ViewCompat.setOnApplyWindowInsetsListener(mRecyclerView, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new ContactAdapter(new ArrayList<>()));
        mReplyTextInputLayout = v.findViewById(R.id.reply_text_input_layout);
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
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            mReplyUnknownSwitch.setEnabled(false);
//            mReplyUnknownSwitch.setVisibility(GONE);
//        }
        mReplyUnknownSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                mReply.setReplyUnknown(isChecked);
//                if (isChecked) {
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//
//
//                        RoleManager roleManager = (RoleManager) getContext().getSystemService(ROLE_SERVICE);
//                        if (roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) && !roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
//                            Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
//                            getActivity().startActivityForResult(intent, PermissionManager.REQUEST_CODE_ROLE_CALL_SCREENING);
//                        }
//
//                    }
//
//                }
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
    public void onResume() {
        super.onResume();
        if (fetch_complete) {
            mRecyclerView.setAdapter(new ContactAdapter(mContacts));
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PRIORITY_REQUEST_CODE:
                if (data != null) {
                    Priority priority = (Priority) data.getSerializableExtra(EXTRA_PRIORITY);
                    mReplyPriority = priority;
                    mReply.setPriority(priority);
                    updateUI();
                }

        }
    }

    private void updateUI() {
        mReplyTextField.setText(mReply.getReply());
        mPriorityTextView.setText(mReply.getPriorityText(getContext()));
    }

    private void getReplyToListAsync() {
        WeakReference<Reply.Callbacks> callbacksWeakReference = new WeakReference<>(EditReplyFragment.this);
        Context context = getContext().getApplicationContext();
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mReply.getReplyToList(context, callbacksWeakReference);
            }
        });
    }

    @Override
    public void onGetSingleContact(Contact contact) {

    }

    @Override
    public void onGetAllContacts(List<Contact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) {
                    mRecyclerView.setAdapter(new ContactAdapter(contacts));
                    mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            mRecyclerView.removeOnLayoutChangeListener(this);
                            if (mReplyTextInputLayout != null) {
                                mReplyTextInputLayout.setHintAnimationEnabled(true);

                            }
                        }
                    });
                } else {
                    fetch_complete = true;
                    mContacts = contacts;
                }
            }
        });
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
        public void add(Contact contact) {
            if (mContacts != null) mContacts.add(contact);
        }
    }
}
