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

public class EditReplyActivity extends SingleFragmentActivity{

    public static final String EXTRA_MODE = "com.undefinedbehaviourgames.callswitch.reply_mode";
    public static final String EXTRA_ID = "com.undefinedbehaviourgames.callswitch.reply_id";
    public static final int EDIT_REPLY = 0;
    public static final int NEW_REPLY = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_CALL_LOG, Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE}, PermissionManager.REQUEST_CODE_CALL_LOG_AND_SEND_SMS);
        }


    }

    @Override
    public Fragment createFragment() {
        int mode = getIntent().getIntExtra(EXTRA_MODE, NEW_REPLY);

        if (mode == EDIT_REPLY) {
            UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
            return EditReplyFragment.newInstance(mode, id);
        }

        return EditReplyFragment.newInstance(NEW_REPLY);
    }

    public static Intent newIntent(Context context, int mode) {

        Intent intent = new Intent(context, EditReplyActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        return intent;
    }

    public static Intent newIntent(Context context, int mode, UUID id) {

        Intent intent = new Intent(context, EditReplyActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_ID,id);
        return intent;
    }
}