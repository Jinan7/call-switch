package com.undefinedbehaviourgames.callswitch;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends BottomNavBarFragment implements ContactQueryHandler.Callbacks {

    private RecyclerView mRecyclerView;
    private RecyclerView mSearchResultRecyclerView;
    private SearchView mSearchView;
    private boolean contactsReady;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contactsReady = false;

    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();

        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        setUpNavBar(v, R.id.menu_contacts);

        mRecyclerView = v.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(new ContactAdapter(ContactLab.getInstance(getContext()).getContacts()));
        mSearchResultRecyclerView = v.findViewById(R.id.contact_search_results);
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchResultRecyclerView.setAdapter(new ContactAdapter(ContactLab.getInstance(getContext()).getContacts()));
        mSearchView = v.findViewById(R.id.contact_search_view);
        mSearchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Contact> searchResults = ContactLab.getInstance(getContext()).getContacts(s.toString());
                ((ContactAdapter) mSearchResultRecyclerView.getAdapter()).setContacts(searchResults);
                mSearchResultRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ContactQueryHandler.getInstance(getContext()).startQuery(ContactsFragment.this);
    }

    @Override
    public void onQueryComplete() {

        ((ContactAdapter) mRecyclerView.getAdapter()).setContacts(ContactLab.getInstance(getContext()).getContacts());
        mRecyclerView.getAdapter().notifyDataSetChanged();


    }

    @Override
    public void onSearchComplete() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case  PermissionManager.REQUEST_CODE_READ_CONTACTS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getActivity().finish();
                }else{

                }
        }
    }

    private int getColor(int id) {
        return ResourcesCompat.getColor(getResources(), id, getActivity().getTheme());
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Contact mContact;
        private TextView mContactName;
        private TextView mContactPhone;
        private TextView mContactIcon;
        private LinearLayout mContactView;
        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            mContactName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactPhone = (TextView) itemView.findViewById(R.id.contact_phone);
            mContactIcon = (TextView) itemView.findViewById(R.id.contact_icon);
            mContactView = (LinearLayout) itemView;
            itemView.setOnClickListener(this);

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

            GradientDrawable cardBackground = (GradientDrawable) itemView.getBackground();
            cardBackground.mutate();

            if (contact.isDeleted()) {
                cardBackground.setColor(getColor(R.color.grey_4));
            } else {
                cardBackground.setColor(getColor(R.color.white));
            }
        }

        @Override
        public void onClick(View v) {
            Intent intent = ContactActivity.newIntent(getContext(), mContact.getId());
            startActivity(intent);
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
            View v = LayoutInflater.from(getContext()).inflate(R.layout.component_contact, parent, false);
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
