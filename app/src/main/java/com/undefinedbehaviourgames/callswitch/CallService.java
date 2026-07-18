package com.undefinedbehaviourgames.callswitch;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CallService extends Service {

    private static final String TAG = "CallServiceLogger";
    private ExecutorService mExecutorService;
    public static final Intent newIntent(Context context) {
        return new Intent(context, CallService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdownNow();
    }

    private final CallServiceBinder mCallServiceBinder = new CallServiceBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mCallServiceBinder;
    }

    public void answerCall(String number) {

        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                String country = tm.getNetworkCountryIso();
                Log.d(TAG, country);
                PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
                try {
                    PhoneNumber phoneProto = phoneNumberUtil.parse(number, country.toUpperCase());
                    Log.d(TAG, phoneProto.toString());
                    Contact contact = ContactLab.getInstance(CallService.this).get(phoneProto);

                    if (contact != null) {
                        String reply = contact.getActiveReplyText(CallService.this);

                        if (!reply.isEmpty()) {
                            sendMessage(reply, contact.getPhone());
                        }
                    }
                } catch (NumberParseException e) {
                    Log.d(TAG, "could not parse number");
                    e.printStackTrace();
                }
            }
        });

    }

    private void sendMessage(String message, String phone) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) return;
        SmsManager manager = SmsManager.getDefault();
        int subscriptionId = SmsManager.getDefaultSmsSubscriptionId();
        manager.sendTextMessage(
                phone,
                null,
                message,
                null,
                null
        );

    }
    public class CallServiceBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }
}
