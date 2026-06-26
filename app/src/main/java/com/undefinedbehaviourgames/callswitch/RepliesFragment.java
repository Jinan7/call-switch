package com.undefinedbehaviourgames.callswitch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class RepliesFragment extends Fragment {

    private RecyclerView mRecyclerView;

    public static RepliesFragment newInstance() {
        RepliesFragment fragment = new RepliesFragment();

        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_replies, container, false);
        mRecyclerView = v.findViewById(R.id.replies_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new RepliesAdapter());
        return v;
    }



    private class RepliesHolder extends RecyclerView.ViewHolder {

        private LinearLayout mReplyButton;
        private TextView mReplyTextView;
        private SwitchMaterial mSwitch;
        public RepliesHolder(@NonNull View itemView) {
            super(itemView);
            mReplyButton = (LinearLayout) itemView.findViewById(R.id.reply_button);
            mReplyTextView = (TextView) itemView.findViewById(R.id.reply_text_view);
            mSwitch = (SwitchMaterial) itemView.findViewById(R.id.toggle_reply);
        }
    }

    private class RepliesAdapter extends RecyclerView.Adapter<RepliesHolder> {

        @NonNull
        @Override
        public RepliesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.component_replies, parent, false);

            return new RepliesHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull RepliesHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 20;
        }
    }
}
