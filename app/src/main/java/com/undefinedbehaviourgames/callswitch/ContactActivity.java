package com.undefinedbehaviourgames.callswitch;

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
    public static Intent newIntent(Context context, String lookupkey ) {
        Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra(EXTRA_LOOKUP_KEY, lookupkey);
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
            return ContactFragment.newInstance(lookupkey);
        } else {
            return ContactFragment.newInstance();
        }

    }

}