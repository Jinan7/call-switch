package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.SelectContactsActivity.READ_CONTACT_REQUEST_CODE;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ContactsFragment extends BottomNavBarFragment implements ContactLabHelper.Callbacks {

    private RecyclerView mRecyclerView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContactLab.getInstance(getContext()).startQuery(ContactsFragment.this);
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
        return v;
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

            case  READ_CONTACT_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    getActivity().finish();
                }else{
                    ContactLab.getInstance(getContext()).startQuery(ContactsFragment.this);
                }
        }
    }

    private class ContactHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Contact mContact;
        private TextView mContactName;
        private TextView mContactPhone;
        private TextView mContactIcon;
        public ContactHolder(@NonNull View itemView) {
            super(itemView);
            mContactName = (TextView) itemView.findViewById(R.id.contact_name);
            mContactPhone = (TextView) itemView.findViewById(R.id.contact_phone);
            mContactIcon = (TextView) itemView.findViewById(R.id.contact_icon);
            itemView.setOnClickListener(this);

        }

        public void bind(Contact contact) {
            mContact = contact;
            mContactName.setText(contact.getName());
            mContactPhone.setText(contact.getPhone());
            mContactIcon.setText(contact.getIcon());
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
