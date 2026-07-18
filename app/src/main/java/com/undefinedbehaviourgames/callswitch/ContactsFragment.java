package com.undefinedbehaviourgames.callswitch;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.search.SearchView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ContactsFragment extends BottomNavBarFragment implements ContactQueryHandler.Callbacks, ContactLabHelper.Callbacks<Contact>, ContactLabHelper.SearchCallbacks<Contact>{

    private RecyclerView mRecyclerView;
    private RecyclerView mSearchResultRecyclerView;
    private SearchView mSearchView;
    private ExecutorService mExecutorService;
    private BottomNavigationView mBottomNavigationView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExecutorService = Executors.newSingleThreadExecutor();
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

//        mBottomNavigationView = v.findViewById(R.id.bottom_nav_view);
        mRecyclerView = v.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        if (ContactQueryHandler.getInstance(getContext()).getQueryState() == State.FETCHED) {
            getContactsAsync();
        }
        mSearchResultRecyclerView = v.findViewById(R.id.contact_search_results);
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchResultRecyclerView.setAdapter(new ContactAdapter(new ArrayList<>()));
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

                WeakReference<ContactLabHelper.SearchCallbacks<Contact  >> callbacksWeakReference = new WeakReference<>(ContactsFragment.this);
                mExecutorService.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                ContactLab.getInstance(getContext()).getContacts(s.toString(), callbacksWeakReference);
                            }
                        }
                );
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContactQueryHandler.getInstance(getContext()).getQueryState() != State.FETCHED) {
            ContactQueryHandler contactQueryHandler = ContactQueryHandler.getInstance(getContext());
            WeakReference<ContactQueryHandler.Callbacks> callbacksWeakReference = new WeakReference<>(ContactsFragment.this);
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    contactQueryHandler.startQuery(callbacksWeakReference);
                }
            });
        }
    }

    @Override
    public void onQueryComplete() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getContactsAsync();
            }
        });
    }


    @Override
    public void onGetSingleContact(Contact contact) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ContactAdapter)mRecyclerView.getAdapter()).add(contact);
                mRecyclerView.getAdapter().notifyItemInserted(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        });
    }

    @Override
    public void onGetAllContacts(List<Contact> contacts) {

    }

    @Override
    public void onSearchResults(List<Contact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ContactAdapter)mSearchResultRecyclerView.getAdapter()).setContacts(contacts);
                mSearchResultRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
    }


    public void getContactsAsync() {

        mRecyclerView.setAdapter(new ContactAdapter(new ArrayList<>()));
        WeakReference<ContactLabHelper.Callbacks<Contact>> callbacksWeakReference = new WeakReference<>(ContactsFragment.this);
        mExecutorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        ContactLab.getInstance(getContext()).getContacts(callbacksWeakReference);
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
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
            Intent intent = ContactActivity.newIntent(getContext(), mContact.getLookupKey());
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
        public void add(Contact contact) {
            if (mContacts != null) mContacts.add(contact);
        }
    }
}
