package com.undefinedbehaviourgames.callswitch;

import static com.undefinedbehaviourgames.callswitch.RepliesFragment.EXTRA_REPLY_INDEX;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.UUID;

public class EditReplyActivity extends SingleFragmentActivity{

    private static final String TAG = "EditReplyActivityLogger";
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

        //make navigation bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(true);

    }

    @Override
    public Fragment createFragment() {
        int mode = getIntent().getIntExtra(EXTRA_MODE, NEW_REPLY);
        int replyIdx = getIntent().getIntExtra(EXTRA_REPLY_INDEX, -1);
        if (mode == EDIT_REPLY) {
            UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
            return EditReplyFragment.newInstance(mode, id, replyIdx);
        }

        return EditReplyFragment.newInstance(NEW_REPLY, replyIdx);
    }

    public static Intent newIntent(Context context, int mode, int replyIdx) {

        Intent intent = new Intent(context, EditReplyActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_REPLY_INDEX, replyIdx);
        return intent;
    }

    public static Intent newIntent(Context context, int mode, UUID id, int replyIdx) {

        Intent intent = new Intent(context, EditReplyActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(EXTRA_ID,id);
        intent.putExtra(EXTRA_REPLY_INDEX, replyIdx);
        return intent;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode ) {

            case PermissionManager.REQUEST_CODE_ROLE_CALL_SCREENING:
                if (requestCode == RESULT_OK) {
                    Log.d(TAG, "Role call screening granted");
                    if (!UnknownCallService.isRunning()) {
                        Log.d(TAG, "starting unknown call service");
                        Intent intent = UnknownCallService.newIntent(this);
                        startService(intent);
                    } else {
                        Log.d(TAG, "service already running");
                    }
                }
        }
    }
}