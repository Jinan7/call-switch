package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.ContactsFragment.EXTRA_CONTACT_INDEX;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class ContactActivity extends SingleFragmentActivity {

    public static final String EXTRA_LOOKUP_KEY = "com.undefinedhbehaviourgames.callswitch.contact_lookupkey";
    public static Intent newIntent(Context context, String lookupkey, int contactIndex ) {
        Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra(EXTRA_LOOKUP_KEY, lookupkey);
        intent.putExtra(EXTRA_CONTACT_INDEX, contactIndex);
        return intent;
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, ContactActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //make status bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        //make navigation bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);

    }

    @Override
    public Fragment createFragment() {

        if (getIntent().hasExtra(EXTRA_LOOKUP_KEY)) {
            String lookupkey  = getIntent().getStringExtra(EXTRA_LOOKUP_KEY);
            int contactIndex = getIntent().getIntExtra(EXTRA_CONTACT_INDEX, -1);
            return ContactFragment.newInstance(lookupkey, contactIndex);
        } else {
            return ContactFragment.newInstance();
        }

    }

}