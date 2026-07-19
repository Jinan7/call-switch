package com.undefinedbehaviourgames.callswitch;

import static android.app.Activity.RESULT_OK;
import static com.undefinedbehaviourgames.callswitch.EditReplyFragment.EXTRA_SELECTED_CONTACTS;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.search.SearchView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelectContactsFragment extends Fragment implements ContactQueryHandler.Callbacks, SelectContactLab.SelectContactCallbacks, ContactLabHelper.SearchCallbacks<SelectContact> {

    public static final String TAG = "SelectContactsFragmentLogger";
    private static final String ARGS_ID = "reply_id";
    private static final String ARGS_SELECTED_CONTACTS = "selected_contacts";
    private RecyclerView mRecyclerView;
    private RecyclerView mSearchResultRecyclerView;
    private SelectContactLab mSelectContactLab;
    private FrameLayout mOptionsLayout;
    private Button mFinishButton;
    private SearchView mSearchView;
    private CheckBox mSelectAllCheckBox;
    private ExecutorService mExecutorService;
    private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            setSelectAllContacts(isChecked);
        }
    };


    private List<Contact> mPrevSelectedContacts;
    public static SelectContactsFragment newInstance(UUID id, ArrayList<Contact> selectedContacts ) {
        SelectContactsFragment fragment = new SelectContactsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARGS_ID, id);
        args.putSerializable(ARGS_SELECTED_CONTACTS, selectedContacts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID id = (UUID) getArguments().getSerializable(ARGS_ID);
        mPrevSelectedContacts = (ArrayList<Contact>) getArguments().getSerializable(ARGS_SELECTED_CONTACTS);
        mSelectContactLab = new SelectContactLab(getContext());
        mSelectContactLab.setPreviousSelectedContacts(mPrevSelectedContacts);
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
    }

    @Override
    public void onResume() {
        super.onResume();
        WeakReference<ContactQueryHandler.Callbacks> callbacksWeakReference = new WeakReference<>(SelectContactsFragment.this);
        ExecutorService executor =  Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ContactQueryHandler.getInstance(getContext()).startQuery(callbacksWeakReference);
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_contacts, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.select_contacts_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchResultRecyclerView = (RecyclerView) v.findViewById(R.id.search_results);
        mSearchResultRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mSearchResultRecyclerView.setAdapter(new SelectContactAdapter(new ArrayList<>()));
        if (ContactQueryHandler.getInstance(getContext()).getQueryState() == State.FETCHED) {
            getContactsAsync();
        }
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
        mSearchView = v.findViewById(R.id.select_contact_search_view);
        mSearchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                List<SelectContact> searchResults = mSelectContactLab.getContacts(s.toString());
//                ((SelectContactAdapter) mSearchResultRecyclerView.getAdapter()).setContacts(searchResults);
//                mSearchResultRecyclerView.getAdapter().notifyDataSetChanged();

                WeakReference<ContactLabHelper.SearchCallbacks<SelectContact>> callbacksWeakReference = new WeakReference<>(SelectContactsFragment.this);
                mExecutorService.execute(
                        new Runnable() {
                            @Override
                            public void run() {
                                mSelectContactLab.getContacts(s.toString(), callbacksWeakReference);
                            }
                        }
                );

            }
        });
        mSearchView.addTransitionListener(new SearchView.TransitionListener() {
            @Override
            public void onStateChanged(@NonNull SearchView searchView, @NonNull SearchView.TransitionState transitionState, @NonNull SearchView.TransitionState transitionState1) {

                if (transitionState1 == SearchView.TransitionState.HIDING) {
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }

            }
        });
        mSelectAllCheckBox = v.findViewById(R.id.select_all);
        mSelectAllCheckBox.setEnabled(false);
        mSelectAllCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
        return v;
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

    public void getContactsAsync() {

        mRecyclerView.setAdapter(new SelectContactAdapter(new ArrayList<>()));
        WeakReference<ContactLabHelper.Callbacks<SelectContact>> callbacksWeakReference = new WeakReference<>(SelectContactsFragment.this);
        mExecutorService.execute(
                new Runnable() {
                    @Override
                    public void run() {
                        mSelectContactLab.getContacts(callbacksWeakReference);
                    }
                }
        );
    }


    public void onFinish() {
        ArrayList<String> selectedContacts = new ArrayList<>();
        selectedContacts = mSelectContactLab.getSelectedContacts();
        Intent result = new Intent();
        result.putExtra(EXTRA_SELECTED_CONTACTS, selectedContacts);
        getActivity().setResult(RESULT_OK, result);
        getActivity().finish();

    }

    private void setSelectAllContacts(boolean isChecked) {
        WeakReference<SelectContactLab.SelectContactCallbacks> callbacksWeakReference = new WeakReference<>(SelectContactsFragment.this);
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                mSelectContactLab.setSelectAllContacts(isChecked, callbacksWeakReference);
            }
        });

//        getContactsAsync();
//        ((SelectContactAdapter) mRecyclerView.getAdapter()).setContacts(mSelectContactLab.getContacts(false));
//        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onSetSelectAllContacts() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SelectContactAdapter) mRecyclerView.getAdapter()).setContacts(mSelectContactLab.getContacts(false));
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });

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

    @Override
    public void onGetSingleContact(SelectContact contact) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SelectContactAdapter)mRecyclerView.getAdapter()).add(contact);
                mRecyclerView.getAdapter().notifyItemInserted(mRecyclerView.getAdapter().getItemCount() - 1);
            }
        });
    }

    @Override
    public void onGetAllContacts(List<SelectContact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSelectContactLab.setContacts(contacts);
            }
        });
        mSelectAllCheckBox.setEnabled(true);
    }

    @Override
    public void onSearchResults(List<SelectContact> contacts) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((SelectContactAdapter)mSearchResultRecyclerView.getAdapter()).setContacts(contacts);
                mSearchResultRecyclerView.getAdapter().notifyDataSetChanged();
            }
        });
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
            mSelectContactCheckBox.setChecked(contact.isChecked());

            GradientDrawable background = (GradientDrawable) mSelectContactIcon.getBackground();
            background.mutate();
            background.setColor(contact.getColor());
            mSelectContactIcon.setTextColor(contact.getSecondaryColor());
        }


        @Override
        public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
            mSelectContact.setChecked(isChecked);

            if (!isChecked) {
                mSelectAllCheckBox.setOnCheckedChangeListener(null);
                mSelectAllCheckBox.setChecked(false);
                mSelectAllCheckBox.setOnCheckedChangeListener(mOnCheckedChangeListener);
            }
        }
    }

    private class SelectContactAdapter extends RecyclerView.Adapter<SelectContactHolder> {

        private List<SelectContact> mContacts;
        public SelectContactAdapter(List<SelectContact> contacts) {

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

        public void setContacts(List<SelectContact> contacts) {
            mContacts = contacts;
        }

        public void add(SelectContact contact) {
            if (mContacts != null) mContacts.add(contact);
        }
    }
}
