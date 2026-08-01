package com.undefinedbehaviourgames.callswitch;


import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    public static final String EXTRA_CONTACT_DELETED = "com.undefinedbehaviourgames.callswitch.contact_deleted";
    public static final String EXTRA_CONTACT_INDEX = "com.undefinedbehaviourgames.callswitch.contact_index";
    private RecyclerView mRecyclerView;
    private RecyclerView mSearchResultRecyclerView;
    private SearchView mSearchView;
    private View mUnknownContactView;
    private ExecutorService mExecutorService;
    private BottomNavigationView mBottomNavigationView;

    boolean fetch_initiated = false;
    boolean fetch_complete = false;
    private List<Contact> mContactList;

    private ActivityResultLauncher<Intent> mLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mExecutorService = Executors.newSingleThreadExecutor();

        if (ContactQueryHandler.getInstance(getContext()).getQueryState() == State.FETCHED) {
            fetch_initiated = true;
            getContactsAsync();
        } else {
            WeakReference<ContactQueryHandler.Callbacks> callbacksWeakReference = new WeakReference<>(ContactsFragment.this);
            ContactQueryHandler.getInstance(getContext()).setCallbacks(callbacksWeakReference);
        }

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                Intent data = result.getData();

                if (data != null) {

                    int index = data.getIntExtra(EXTRA_CONTACT_INDEX, -1);

                    String lookupKey = data.getStringExtra(EXTRA_CONTACT_DELETED);

                    if (lookupKey != null) {

                        if (index != -1) {
                            ((ContactAdapter) mRecyclerView.getAdapter()).delete(index, lookupKey);
                        }
                    }

                }
            }
        });
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();

        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contacts, container, false);
        setUpNavBar(v, R.id.nav_contacts);

//        mBottomNavigationView = v.findViewById(R.id.bottom_nav_view);
        mRecyclerView = v.findViewById(R.id.contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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
        mUnknownContactView = v.findViewById(R.id.unknown_contacts_view);
        mUnknownContactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ContactActivity.newIntent(getContext());
                startActivity(intent);
            }
        });
        if (!fetch_complete) {
            mUnknownContactView.setVisibility(INVISIBLE);
        }
        updateUnknownContactViewUI();
        return v;
    }

    private void updateUnknownContactViewUI() {
        TextView iconView = (TextView) mUnknownContactView.findViewById(R.id.contact_icon);
        TextView nameView = (TextView) mUnknownContactView.findViewById(R.id.contact_name);
        TextView phoneView = (TextView) mUnknownContactView.findViewById(R.id.contact_phone);


        iconView.setText(R.string.unknown_contacts_icon_label);
        nameView.setText(getString(R.string.unknown_contacts_label));
        phoneView.setText(getString(R.string.unknown_contacts_phone_label));
    }
    @Override
    public void onResume() {
        super.onResume();
        if (fetch_complete) {
            mUnknownContactView.setVisibility(VISIBLE);
            mRecyclerView.setAdapter(new ContactAdapter(mContactList));
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
    }

    @Override
    public void onGetAllContacts(List<Contact> contacts) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) {
                    mUnknownContactView.setVisibility(VISIBLE);
                    mRecyclerView.setAdapter(new ContactAdapter(contacts));
                } else {
                    mContactList = contacts;
                    fetch_complete = true;
                }
            }
        });

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
            Intent intent = ContactActivity.newIntent(getContext(), mContact.getLookupKey(), getBindingAdapterPosition());
            mLauncher.launch(intent);
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

        public void delete(int position, String lookupKey) {
            if (mContacts == null) return;
            if (position >= mContacts.size()) return;

            if (lookupKey.equals(mContacts.get(position).getLookupKey())) {
                mContacts.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
}
