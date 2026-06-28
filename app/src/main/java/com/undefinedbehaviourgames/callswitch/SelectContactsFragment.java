package com.undefinedbehaviourgames.callswitch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SelectContactsFragment extends Fragment {

    public static SelectContactsFragment newInstance() {
        SelectContactsFragment fragment = new SelectContactsFragment();

        return fragment;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_contacts, container, false);
        return v;
    }
}
