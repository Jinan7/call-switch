package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.OutOfQuotaPolicy;
import androidx.work.WorkManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiverLogger";
    private String number;
    private CallService mCallService;
    private CallServiceConnection mCallServiceConnection = new CallServiceConnection();
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            //if call log permission is not granted, number cannot be read
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) return;
            //if the system is turned off getSystemSettings return false
            if (!SettingsPreferences.getSystemSettings(context)) return;



            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "Incoming call from " + number);

                OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(CallWorker.class)
                        .setInputData(CallWorker.newData(number))
                        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        .build();

                WorkManager.getInstance(context).enqueue(request);
            }
        }
    }

    private class CallServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mCallService = ((CallService.CallServiceBinder)service).getService();
            mCallService.answerCall(number);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mCallService = null;
        }
    }
}


