package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.ContactsFragment.EXTRA_CONTACT_DELETED;
import static com.undefinedbehaviourgames.callswitch.ContactsFragment.EXTRA_CONTACT_INDEX;
import static com.undefinedbehaviourgames.callswitch.Priority.HIGH;
import static com.undefinedbehaviourgames.callswitch.Priority.LOW;
import static com.undefinedbehaviourgames.callswitch.Priority.NORMAL;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_DELETED;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_INDEX;
import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_UPDATED;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactFragment extends Fragment implements Contact.CallBacks {
    private static final String TAG = "ContactFragmentLogger";
    private static final String ARG_LOOKUPKEY = "contact_id";
    private static final String ARG_CONTACT_INDEX = "contact_index";
    public static final String EXTRA_UPDATE_ACTIVE_REPLY = "com.undefinedbehaviourgames.callswitch.update_active_reply";
    private int contactIndex = -1;
    private MaterialToolbar mToolbar;
    private TextView mContactIconTextView;
    private TextView mContactNameTextView;
    private TextView mContactPhoneTextView;
    private TextView mContactActiveReplyTextView;
    private FrameLayout mPriorityIcon;
    private RecyclerView mRecyclerView;
    private ImageButton mEditActiveReply;
    private Contact mContact;
    private ExecutorService mExecutorService;
    private boolean fetch_complete = false;
    private List<Reply> mReplies;
    private boolean unknownContact;

    ActivityResultLauncher<Intent> mLauncher;

    public static ContactFragment newInstance(String lookupkey, int contactIndex) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOOKUPKEY, lookupkey);
        args.putInt(ARG_CONTACT_INDEX, contactIndex);
        fragment.setArguments(args);
        return fragment;
    }

    public static ContactFragment newInstance() {
        return new ContactFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                Intent data = result.getData();

                if (data != null) {

                    ReplyLab.getInstance(getContext()).setUnreachableUpdate(true);
                    int index = data.getIntExtra(EXTRA_REPLY_INDEX, -1);

                    UUID updateReplyId = (UUID) data.getSerializableExtra(EXTRA_REPLY_UPDATED);
                    if (updateReplyId != null) {
                        Reply reply = ReplyLab.getInstance(getContext()).get(updateReplyId);
                        if (index == -1) {
                            updateActiveReplyUI();
                        } else {
                            ((RepliesAdapter) mRecyclerView.getAdapter()).updateReply(index, reply);
                        }
                    }

                    UUID deletedReplyId = (UUID) data.getSerializableExtra(EXTRA_REPLY_DELETED);
                    if (deletedReplyId != null) {
                        Reply reply = ReplyLab.getInstance(getContext()).get(deletedReplyId);
                        if (index == -1) {
                            updateActiveReplyUI();
                        } else {
                            ((RepliesAdapter) mRecyclerView.getAdapter()).deleteReply(index, deletedReplyId);
                        }
                    }
                }
            }
        });

        if (getArguments() != null) {
            String contactLookup = getArguments().getString(ARG_LOOKUPKEY);
            contactIndex = getArguments().getInt(ARG_CONTACT_INDEX);
            mContact = ContactLab.getInstance(getContext()).get(contactLookup);
            unknownContact = false;
        } else {
            mContact = ContactLab.getInstance(getContext()).getUnknownContact(getContext());
            unknownContact = true;
        }

        mExecutorService = Executors.newSingleThreadExecutor();
        getRepliesAsync();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fetch_complete) {
            mRecyclerView.setAdapter(new RepliesAdapter(mReplies));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        mToolbar = (MaterialToolbar) v.findViewById(R.id.contact_toolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.contact_menu_delete) {

                    Intent intent = new Intent();
                    intent.putExtra(EXTRA_CONTACT_INDEX, contactIndex);
                    intent.putExtra(EXTRA_CONTACT_DELETED, mContact.getLookupKey());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            ContactLab.getInstance(getContext()).delete(getContext(), mContact);
                        }
                    });

                    getActivity().finish();
                }
                return false;
            }
        });

        if (unknownContact) mToolbar.getMenu().clear();
        mContactIconTextView = v.findViewById(R.id.contact_contact_icon);
        mContactNameTextView = v.findViewById(R.id.contact_contact_name);
        mContactPhoneTextView = v.findViewById(R.id.contact_contact_phone);
        mContactActiveReplyTextView = v.findViewById(R.id.contact_active_reply);
        mPriorityIcon = v.findViewById(R.id.contact_reply_priority_button);
        mRecyclerView = v.findViewById(R.id.contact_replies_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ViewCompat.setOnApplyWindowInsetsListener(mRecyclerView, new OnApplyWindowInsetsListener() {
            @Override
            public @org.jspecify.annotations.NonNull WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                v.setPadding(v.getLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
                return insets;
            }
        });
        mEditActiveReply = v.findViewById(R.id.contact_edit_active_reply);
        mEditActiveReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mContact.getActiveReplyId(getContext()) == null) return;
                Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.EDIT_REPLY, mContact.getActiveReplyId(getContext()), -1);
                mLauncher.launch(intent);
            }
        });


        updateUI();
        return v;
    }

    private void getRepliesAsync() {

        WeakReference<Contact.CallBacks> callbackWeakReference = new WeakReference(ContactFragment.this);

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mContact.getReplies(getContext(), callbackWeakReference);
            }
        });
    }
    @Override
    public void onGetReplies(List<Reply> replies) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) {
                    mRecyclerView.setAdapter(new RepliesAdapter(replies));
                } else {
                    fetch_complete = true;
                    mReplies = replies;
                }
            }
        });
    }

    private void updateActiveReplyUI() {
        mContactActiveReplyTextView.setText(mContact.getActiveReplyText(getContext()));

        LayerDrawable stateBackground = (LayerDrawable) getResources().getDrawable(R.drawable.circle_background_with_state);
        mPriorityIcon.setBackground(stateBackground);
//
        GradientDrawable background = (GradientDrawable) stateBackground.findDrawableByLayerId(R.id.circle_background);
        GradientDrawable state = (GradientDrawable) stateBackground.findDrawableByLayerId(R.id.state_circle_background);
        GradientDrawable altBackground = (GradientDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.circle_background_stroke_2, getContext().getTheme());
        background.mutate();
        state.mutate();
        int stroke_width = (int)getResources().getDimension(R.dimen.circle_stroke_2);

        if (mContact.isActiveReplyEnabled(getContext())) {
            state.setColor(getColor(R.color.online_green));
        } else {
            state.setColor(getColor(R.color.grey_7));
        }
        switch (mContact.getActiveReplyPriority(getContext())) {

            case LOW:
                background.setColor(getColor(R.color.priority_green_2));
                background.setStroke(stroke_width, getColor(R.color.priority_green_3));
                break;
            case NORMAL:
                background.setColor(getColor(R.color.priority_blue_1));
                background.setStroke(stroke_width, getColor(R.color.priority_blue_2));
                break;
            case HIGH:
                background.setColor(getColor(R.color.priority_red_1));
                background.setStroke(stroke_width, getColor(R.color.priority_red_3));
                break;
            default:
                //default case occurs when there is no active reply
                //in that case use alternative background without state
                mPriorityIcon.setBackground(altBackground);
                break;

        }
    }
    private void updateUI() {
        mContactNameTextView.setText(mContact.getName());
        mContactIconTextView.setText(mContact.getIcon());
        mContactPhoneTextView.setText(mContact.getPhone());

        updateActiveReplyUI();

        GradientDrawable contactIconBackground = (GradientDrawable) mContactIconTextView.getBackground();
        contactIconBackground.mutate();
        contactIconBackground.setColor(mContact.getColor());
        mContactIconTextView.setTextColor(mContact.getSecondaryColor());


    }

    private int getColor(int id) {
        return getResources().getColor(id, getContext().getTheme());
    }


    private class RepliesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, PopupMenu.OnMenuItemClickListener {

        private Reply mReply;
        private FrameLayout mReplyPriorityButton;
        private TextView mReplyTextView;
        private ImageButton mMenuOptionsButton;
        public RepliesHolder(@NonNull View itemView) {
            super(itemView);
            mReplyPriorityButton = (FrameLayout) itemView.findViewById(R.id.contact_reply_priority_button);
            mReplyTextView = (TextView) itemView.findViewById(R.id.contact_reply_text_view);
            mMenuOptionsButton = (ImageButton) itemView.findViewById(R.id.contact_reply_menu_options);
            mMenuOptionsButton.setOnClickListener(this);
//            mReplyPriorityButton.setOnClickListener(this);
//            mReplyTextView.setOnClickListener(this);

        }

        public void bind(Reply reply) {
            mReply = reply;
            mReplyTextView.setText(reply.getReply());
            GradientDrawable background = (GradientDrawable) mReplyPriorityButton.getBackground();
            background.mutate();
            int stroke_width = (int)getResources().getDimension(R.dimen.circle_stroke_2);
            switch (mReply.getPriority()) {

                case LOW:

                    background.setColor(getColor(R.color.priority_green_2));
                    background.setStroke(stroke_width, getColor(R.color.priority_green_3));
                    break;
                case NORMAL:
                    background.setColor(getColor(R.color.priority_blue_1));
                    background.setStroke(stroke_width, getColor(R.color.priority_blue_2));
                    break;
                case HIGH:
                    background.setColor(getColor(R.color.priority_red_1));
                    background.setStroke(stroke_width, getColor(R.color.priority_red_3));
                    break;

            }
        }

        @Override
        public void onClick(View v) {
            PopupMenu popup = new PopupMenu(getContext(), v);
            popup.getMenuInflater().inflate(R.menu.contact_reply_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();

        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {

            if (item.getItemId() ==R.id.contact_menu_make_active) {

                int swapPosition = getBindingAdapterPosition();
                ((RepliesAdapter) mRecyclerView.getAdapter()).remove(swapPosition);
                Reply reply = ReplyLab.getInstance(getContext()).get(mContact.getActiveReplyId());
                ((RepliesAdapter) mRecyclerView.getAdapter()).add(swapPosition, reply);
                mContact.setActiveReplyId(mReply.getId());
                ContactLab.getInstance(getContext()).update(mContact);
                updateUI();
                return true;
            } else if (item.getItemId() == R.id.contact_menu_edit_reply) {
                Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.EDIT_REPLY, mReply.getId(), getBindingAdapterPosition());
                mLauncher.launch(intent);
                return true;
            }
            return false;
        }

        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            mReply.setEnabled(isChecked);
        }
    }

    private class RepliesAdapter extends RecyclerView.Adapter<RepliesHolder> {

        List<Reply> mReplies;

        public RepliesAdapter(List<Reply> replies) {
            mReplies = replies;
        }
        @NonNull
        @Override
        public RepliesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.component_contact_reply, parent, false);

            return new RepliesHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RepliesHolder holder, int position) {
            holder.bind(mReplies.get(position));
        }

        @Override
        public int getItemCount() {
            return mReplies.size();
        }

        public void setReplies(List<Reply> replies) {
            mReplies = replies;
        }

        public void add(int position, Reply reply) {
            if (reply == null) return;
            if (mReplies == null) return;

            if (position <= mReplies.size()) {
                mReplies.add(position, reply);
                notifyItemInserted(position);
            }else {
                mReplies.add(reply);
                notifyItemInserted(mReplies.size() -1);
            }


        }

        private void remove(int position) {
            if (mReplies == null) return;
            if (position >= mReplies.size()) return;
            mReplies.remove(position);
            notifyItemRemoved(position);
        }
        public void updateReply(int position, Reply reply) {
            if (mReplies == null) return;
            if (position >= mReplies.size()) return ;

            if (reply.getId().equals(mReplies.get(position).getId())) {
                mReplies.set(position, reply);
                notifyItemChanged(position);
            }
        }

        public void deleteReply(int position, UUID replyId) {
            if (mReplies == null) return;
            if (position >= mReplies.size()) return ;

            if (replyId.equals(mReplies.get(position).getId())) {
                mReplies.remove(position);
                notifyItemRemoved(position);
            }
        }

        public void updateReply(Reply reply) {
            if (mReplies == null) return;

            for (int i=0; i< mReplies.size(); i++) {
                if (mReplies.get(i).getId().equals(reply.getId())) {
                    mReplies.set(i, reply);
                    notifyItemChanged(i);
                    break;
                }
            }

        }

        public void deleteReply(UUID id) {
            if (mReplies == null) return;

            for (int i=0; i< mReplies.size(); i++) {
                if (mReplies.get(i).getId().equals(id)) {
                    mReplies.remove(i);
                    notifyItemRemoved(i);
                    break;
                }
            }
        }
    }
}
