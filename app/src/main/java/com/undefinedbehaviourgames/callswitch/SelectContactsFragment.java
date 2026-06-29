package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.SelectContactsActivity.READ_CONTACT_REQUEST_CODE;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectContactsFragment extends Fragment implements SelectContactLab.Callbacks {

    private RecyclerView mRecyclerView;
    private SelectContactLab mSelectContactLab;
    public static SelectContactsFragment newInstance() {
        SelectContactsFragment fragment = new SelectContactsFragment();

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return v;
    }

    @Override
    public void onQueryComplete() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
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

    private class SelectContactHolder extends RecyclerView.ViewHolder {

        private SelectContact mSelectContact;
        private TextView mSelectContactName;
        private TextView mSelectContactPhone;
        private TextView mSelectContactIcon;
        public SelectContactHolder(@NonNull View itemView) {
            super(itemView);
            mSelectContactName = itemView.findViewById(R.id.select_contact_name);
            mSelectContactPhone = itemView.findViewById(R.id.select_contact_phone);
            mSelectContactIcon = itemView.findViewById(R.id.select_contact_icon);
        }

        public void bind(SelectContact contact) {
            mSelectContact = contact;
            mSelectContactName.setText(contact.getName());
            mSelectContactPhone.setText(contact.getPhone());
            mSelectContactIcon.setText(contact.getIcon());
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
