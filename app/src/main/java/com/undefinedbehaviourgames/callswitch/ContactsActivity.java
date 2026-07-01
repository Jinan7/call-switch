package com.undefinedbehaviourgames.callswitch;

import android.content.Context;
import android.content.Intent;

import androidx.fragment.app.Fragment;

public class ContactsActivity extends SingleFragmentActivity{

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ContactsActivity.class);
        return intent;
    }
    @Override
    public Fragment createFragment() {
        return ContactsFragment.newInstance();
    }
}
