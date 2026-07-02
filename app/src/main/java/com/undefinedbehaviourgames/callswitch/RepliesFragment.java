package com.undefinedbehaviourgames.callswitch;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.List;

public class RepliesFragment extends BottomNavBarFragment {

    private static final String TAG = "RepliesFragmentLogger";
    private RecyclerView mRecyclerView;
    private MaterialToolbar mToolbar;

    public static RepliesFragment newInstance() {
        RepliesFragment fragment = new RepliesFragment();

        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_replies, container, false);
        setUpNavBar(v, R.id.menu_replies);
        mRecyclerView = v.findViewById(R.id.replies_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RepliesAdapter(ReplyLab.getInstance(getContext()).getReplies()));
        mToolbar = v.findViewById(R.id.replies_toolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.add_reply) {

                    Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.NEW_REPLY);
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });


        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        ((RepliesAdapter)mRecyclerView.getAdapter()).setReplies(ReplyLab.getInstance(getContext()).getReplies());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        return super.onContextItemSelected(item);
    }

    private class RepliesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private Reply mReply;
        private FrameLayout mReplyPriorityButton;
        private TextView mReplyTextView;
        private SwitchMaterial mSwitch;
        public RepliesHolder(@NonNull View itemView) {
            super(itemView);
            mReplyPriorityButton = (FrameLayout) itemView.findViewById(R.id.reply_priority_button);
            mReplyTextView = (TextView) itemView.findViewById(R.id.reply_text_view);
            mSwitch = (SwitchMaterial) itemView.findViewById(R.id.toggle_reply);
            mReplyPriorityButton.setOnClickListener(this);
            mReplyTextView.setOnClickListener(this);

        }

        public void bind(Reply reply) {
            mReply = reply;
            mReplyTextView.setText(reply.getReply());
            GradientDrawable background = (GradientDrawable) mReplyPriorityButton.getBackground();
            background.mutate();
            int stroke_width = (int)getResources().getDimension(R.dimen.circle_stroke_2);
            switch (mReply.getPriority()) {

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

            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.EDIT_REPLY, mReply.getId());
            startActivity(intent);
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
            View v = LayoutInflater.from(getContext()).inflate(R.layout.component_replies, parent, false);

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
