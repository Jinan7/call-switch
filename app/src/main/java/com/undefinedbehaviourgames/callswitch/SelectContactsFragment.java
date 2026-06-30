package com.undefinedbehaviourgames.callswitch;

import static android.app.Activity.RESULT_OK;
import static com.undefinedbehaviourgames.callswitch.EditReplyFragment.EXTRA_SELECTED_CONTACTS;
import static com.undefinedbehaviourgames.callswitch.SelectContactsActivity.READ_CONTACT_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SelectContactsFragment extends Fragment implements SelectContactLab.Callbacks {

    private static final String ARGS_ID = "reply_id";
    private RecyclerView mRecyclerView;
    private SelectContactLab mSelectContactLab;
    private FrameLayout mOptionsLayout;
    private Button mFinishButton;
    private Reply mReply;
    public static SelectContactsFragment newInstance(UUID id) {
        SelectContactsFragment fragment = new SelectContactsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID) getArguments().getSerializable(ARGS_ID);
        mReply = ReplyLab.getInstance().get(id);
        mSelectContactLab = new SelectContactLab(getContext(), this);
        mSelectContactLab.startQuery();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_select_contacts, container, false);
        mRecyclerView = v.findViewById(R.id.select_contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new SelectContactAdaper(mSelectContactLab.getContacts()));
        mOptionsLayout =(FrameLayout) v.findViewById(R.id.options);
        ViewCompat.setOnApplyWindowInsetsListener(mOptionsLayout, new OnApplyWindowInsetsListener() {
            @Override
            public @org.jspecify.annotations.NonNull WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                v.setPadding(v.getLeft(), v.getPaddingTop(), v.getPaddingRight(), insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom);
                return insets;
            }
        });
        mFinishButton = v.findViewById(R.id.select_contacts_finish);
        mFinishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFinish();
            }
        });
        return v;
    }

    @Override
    public void onQueryComplete() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    public void onFinish() {
        ArrayList<Contact> selectedContacts = new ArrayList<>();
        selectedContacts = mSelectContactLab.getSelectedContacts();
        Intent result = new Intent();
        result.putExtra(EXTRA_SELECTED_CONTACTS, selectedContacts);
        getActivity().setResult(RESULT_OK, result);
        getActivity().finish();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSelectContactLab.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case  READ_CONTACT_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getActivity().finish();
                }else{
                    if (mSelectContactLab != null) mSelectContactLab.startQuery();
                }
        }
    }

    private class SelectContactHolder extends RecyclerView.ViewHolder implements CompoundButton.OnCheckedChangeListener {

        private SelectContact mSelectContact;
        private TextView mSelectContactName;
        private TextView mSelectContactPhone;
        private TextView mSelectContactIcon;
        private CheckBox mSelectContactCheckBox;
        public SelectContactHolder(@NonNull View itemView) {
            super(itemView);
            mSelectContactName = (TextView) itemView.findViewById(R.id.select_contact_name);
            mSelectContactPhone = (TextView) itemView.findViewById(R.id.select_contact_phone);
            mSelectContactIcon = (TextView) itemView.findViewById(R.id.select_contact_icon);
            mSelectContactCheckBox = (CheckBox) itemView.findViewById(R.id.select_contact_checkbox);
            mSelectContactCheckBox.setOnCheckedChangeListener(this);
        }

        public void bind(SelectContact contact) {
            mSelectContact = contact;
            mSelectContactName.setText(contact.getName());
            mSelectContactPhone.setText(contact.getPhone());
            mSelectContactIcon.setText(contact.getIcon());
        }


        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            mSelectContact.setChecked(isChecked);
        }
    }

    private class SelectContactAdaper extends RecyclerView.Adapter<SelectContactHolder> {

        private List<SelectContact> mContacts;
        public SelectContactAdaper(List<SelectContact> contacts) {

            mContacts = contacts;
        }


        @NonNull
        @Override
        public SelectContactHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.component_select_contact, parent, false);
            return new SelectContactHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectContactHolder holder, int position) {
            holder.bind(mContacts.get(position));
        }

        @Override
        public int getItemCount() {
            return mContacts.size();
        }
    }
}
