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
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class ContactActivity extends SingleFragmentActivity {

    public static final String EXTRA_ID = "com.undefinedhbehaviourgames.callswitch.contact_id";
    public static Intent newIntent(Context context, Long id ) {
        Intent intent = new Intent(context, ContactActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public Fragment createFragment() {
        Long id  = getIntent().getLongExtra(EXTRA_ID, -1);
        return ContactFragment.newInstance(id);
    }

}