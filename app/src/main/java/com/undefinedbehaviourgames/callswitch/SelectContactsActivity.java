package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SelectContactsActivity extends SingleFragmentActivity {

    public static final int READ_CONTACT_REQUEST_CODE = 0;
    public static Intent newIntent(Context context) {

        Intent intent = new Intent(context, SelectContactsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String [] {Manifest.permission.READ_CONTACTS}, READ_CONTACT_REQUEST_CODE);
        }
    }

    @Override
    public Fragment createFragment() {
        return SelectContactsFragment.newInstance();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);

        switch (requestCode) {
            case  READ_CONTACT_REQUEST_CODE:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }

                FragmentManager fm = getSupportFragmentManager();

                SelectContactsFragment fragment = (SelectContactsFragment) fm.findFragmentById(R.id.main);

                if (fragment != null) {
                    fragment.onRequestPermissionsResult(READ_CONTACT_REQUEST_CODE, permissions, grantResults);
                }
        }
    }
}