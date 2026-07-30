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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RepliesFragment extends BottomNavBarFragment implements ReplyLab.Callbacks {

    private static final String TAG = "RepliesFragmentLogger";
    private RecyclerView mRecyclerView;
    private MaterialToolbar mToolbar;
    private MaterialSwitch mToggleAllReplies;
    private ExecutorService mExecutorService;
    private ExecutorService mReplyExecutorService;
    private Future<Object> mGetRepliesFuture;
    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            setEnabledAllReplies(isChecked);

        }
    };

    public static RepliesFragment newInstance() {
        RepliesFragment fragment = new RepliesFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExecutorService = Executors.newSingleThreadExecutor();
        mReplyExecutorService = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_replies, container, false);
        setUpNavBar(v, R.id.nav_replies);
        mRecyclerView = v.findViewById(R.id.replies_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RepliesAdapter(new ArrayList<>()));
        mToolbar = v.findViewById(R.id.replies_toolbar);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                if (item.getItemId() == R.id.add_reply) {

                    Intent intent = EditReplyActivity.newIntent(getContext(), EditReplyActivity.NEW_REPLY);
                    startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.settings) {
                    Intent intent = SettingsActivity.newIntent(getContext());
                    startActivity(intent);
                }
                return false;
            }
        });

        mToggleAllReplies = v.findViewById(R.id.toggle_all);
        mToggleAllReplies.setEnabled(false);
        mToggleAllReplies.setOnCheckedChangeListener(mOnCheckedChangeListener);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();
        getRepliesAsync();
    }

    private void getRepliesAsync() {
        if (mGetRepliesFuture != null) {
            mGetRepliesFuture.cancel(true);
        }
        mRecyclerView.setAdapter(new RepliesAdapter(new ArrayList<>()));
        WeakReference<ReplyLab.Callbacks> callbacksWeakReference = new WeakReference<>(RepliesFragment.this);
        mGetRepliesFuture = mExecutorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {

                ReplyLab.getInstance(getContext()).getReplies(callbacksWeakReference, mGetRepliesFuture);
                return null;
            }
        });

    }

    @Override
    public void ongetSingleReply(Reply reply) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((RepliesAdapter) mRecyclerView.getAdapter()).add(reply);
                mRecyclerView.getAdapter().notifyItemInserted(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        });
    }

    @Override
    public void onGetAllReplies() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mToggleAllReplies.setEnabled(true);
            }
        });

    }

    @Override
    public void onUpdateReplies(List<Reply> replies) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getRepliesAsync();
            }
        });
    }

    @Override
    public void onUpdateReply(int index) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (index < mRecyclerView.getAdapter().getItemCount()) mRecyclerView.getAdapter().notifyItemChanged(index);
            }
        });
    }

    private void setEnabledAllReplies(boolean isChecked) {
        setEnableAllRepliesAsync(isChecked);

    }

    private void setEnableAllRepliesAsync(boolean isChecked) {
        WeakReference<ReplyLab.Callbacks> callbacksWeakReference = new WeakReference<>(RepliesFragment.this);
        mReplyExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                ReplyLab.getInstance(getContext()).setEnabledAllReplies(getContext(), isChecked, callbacksWeakReference);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
    }



    private class RepliesHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        private Reply mReply;
        private FrameLayout mReplyPriorityButton;
        private TextView mReplyTextView;
        private MaterialSwitch mSwitch;
        public RepliesHolder(@NonNull View itemView) {
            super(itemView);
            mReplyPriorityButton = (FrameLayout) itemView.findViewById(R.id.reply_priority_button);
            mReplyTextView = (TextView) itemView.findViewById(R.id.reply_text_view);
            mSwitch =  itemView.findViewById(R.id.toggle_reply);
            mSwitch.setOnCheckedChangeListener(this);
            mReplyPriorityButton.setOnClickListener(this);
            mReplyTextView.setOnClickListener(this);

        }

        public void bind(Reply reply) {
            mReply = reply;
            mReplyTextView.setText(reply.getReply());
            mSwitch.setOnCheckedChangeListener(null);
            mSwitch.setChecked(mReply.isEnabled());
            mSwitch.setOnCheckedChangeListener(this);
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
            updateReplyAsync();
            if (!isChecked) {

                mToggleAllReplies.setOnCheckedChangeListener(null);
                mToggleAllReplies.setChecked(isChecked);
                mToggleAllReplies.setOnCheckedChangeListener(mOnCheckedChangeListener);

            }
        }

        public void updateReplyAsync() {
            mReplyExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    ReplyLab.getInstance(getContext()).update(getContext(), mReply);
                }
            });
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
        public void add(Reply reply) {
            if (mReplies != null) {
                mReplies.add(reply);
            }
        }
    }
}
