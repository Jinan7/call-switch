package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SelectContactsActivity extends SingleFragmentActivity {

    private static final String EXTRA_ID = "com.undefinedbehaviourgames.callswitch.extra_reply_id";
    private static final String EXTRA_SELECTED_CONTACTS = "com.undefinedbehaviourgames.callswitch.extra_selected_contacts";
    public static Intent newIntent(Context context, UUID id, List<Contact> selectedContacts) {

        Intent intent = new Intent(context, SelectContactsActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SELECTED_CONTACTS, (ArrayList<Contact>) selectedContacts);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.READ_CONTACTS}, PermissionManager.REQUEST_CODE_READ_CONTACTS);
        }

        //make nav system bar transparent or make is same color
        //as nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(true);
        } else {
            getWindow().setNavigationBarColor(getColor(R.color.white));
        }
        //make status bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);
    }

    @Override
    public Fragment createFragment() {
        UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
        @SuppressWarnings("unchecked")
        ArrayList<Contact> selectedContacts = (ArrayList<Contact>) getIntent().getSerializableExtra(EXTRA_SELECTED_CONTACTS);
        return SelectContactsFragment.newInstance(id, selectedContacts);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        switch (requestCode) {
            case  PermissionManager.REQUEST_CODE_READ_CONTACTS:


                FragmentManager fm = getSupportFragmentManager();

                SelectContactsFragment fragment = (SelectContactsFragment) fm.findFragmentById(R.id.main);

                if (fragment != null) {
                    fragment.onRequestPermissionsResult(PermissionManager.REQUEST_CODE_READ_CONTACTS, permissions, grantResults);
                }
        }
    }
}