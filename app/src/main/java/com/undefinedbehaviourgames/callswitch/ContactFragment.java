package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.Priority.HIGH;
import static com.undefinedbehaviourgames.callswitch.Priority.LOW;
import static com.undefinedbehaviourgames.callswitch.Priority.NORMAL;

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

import java.util.List;

public class ContactFragment extends Fragment {
    private static final String TAG = "ContactFragmentLogger";
    private static final String ARG_LOOKUPKEY = "contact_id";
    private MaterialToolbar mToolbar;
    private TextView mContactIconTextView;
    private TextView mContactNameTextView;
    private TextView mContactPhoneTextView;
    private TextView mContactActiveReplyTextView;
    private FrameLayout mPriorityIcon;
    private RecyclerView mRecyclerView;
    private ImageButton mEditActiveReply;
    private Contact mContact;

    public static ContactFragment newInstance(String lookupkey) {
        ContactFragment fragment = new ContactFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LOOKUPKEY, lookupkey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String contactLookup = getArguments().getString(ARG_LOOKUPKEY);
        mContact = ContactLab.getInstance(getContext()).get(contactLookup);
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
                    ContactLab.getInstance(getContext()).delete(getContext(), mContact);
                    getActivity().finish();
                }
                return false;
            }
        });
        mContactIconTextView = v.findViewById(R.id.contact_contact_icon);
        mContactNameTextView = v.findViewById(R.id.contact_contact_name);
        mContactPhoneTextView = v.findViewById(R.id.contact_contact_phone);
        mContactActiveReplyTextView = v.findViewById(R.id.contact_active_reply);
        mPriorityIcon = v.findViewById(R.id.contact_reply_priority_button);
        mRecyclerView = v.findViewById(R.id.contact_replies_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RepliesAdapter(mContact.getReplies(getContext())));
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
                Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.EDIT_REPLY, mContact.getActiveReplyId(getContext()));
                startActivity(intent);
            }
        });


        updateUI();
        return v;
    }



    private void updateUI() {
        mContactNameTextView.setText(mContact.getName());
        mContactIconTextView.setText(mContact.getIcon());
        mContactPhoneTextView.setText(mContact.getPhone());
        mContactActiveReplyTextView.setText(mContact.getActiveReplyText(getContext()));

        LayerDrawable stateBackground = (LayerDrawable) mPriorityIcon.getBackground();
//        LayerDrawable stateBackground = (LayerDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.circle_background_with_state, getContext().getTheme());
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
                mContact.setActiveReplyId(mReply.getId());
                ContactLab.getInstance(getContext()).update(mContact);
                updateUI();
                return true;
            } else if (item.getItemId() == R.id.contact_menu_edit_reply) {
                Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.EDIT_REPLY, mReply.getId());
                startActivity(intent);
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
    }
}
