package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class ContactsActivity extends SingleFragmentActivity{

    public static final int READ_CONTACT_REQUEST_CODE = 0;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ContactsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public Fragment createFragment() {
        return ContactsFragment.newInstance();
    }


}
