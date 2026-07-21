package com.undefinedbehaviourgames.callswitch;

import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnknownCallService extends CallScreeningService {
    private static final String TAG = "UnknownCallServiceLogger";
    private static boolean sRunning = false;
    private ExecutorService mExecutorService;

    public static Intent newIntent(Context context) {
        return new Intent(context, UnknownCallService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sRunning = false;
        mExecutorService.shutdownNow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sRunning = true;
        return START_REDELIVER_INTENT;
    }



    @Override
    public void onScreenCall(@NonNull Call.Details callDetails) {

        Log.d(TAG, "screening call");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return;

        boolean isIncoming = callDetails.getCallDirection() == Call.Details.DIRECTION_INCOMING;

        if (isIncoming) {
            Uri handle = callDetails.getHandle();
            Log.d(TAG, handle.getSchemeSpecificPart());

            if (handle.getSchemeSpecificPart() != null) {
                mExecutorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        answerCall(handle.getSchemeSpecificPart());
                    }
                });
            }

        }
    }

    public static boolean isRunning() {
        return sRunning;
    }

    public static void setRunning(boolean running) {
        sRunning = running;
    }

    public void answerCall(String number) {

        UUID id = ContactPreferences.getUnknownContact(getApplicationContext()).getActiveReplyId();
        Reply reply = ReplyLab.getInstance(this).get(id);
        if (reply != null && reply.isEnabled()) {
            sendMessage(reply.getReply(), number);
        }

    }
    private void sendMessage(String message, String phone) {
        if (message.isEmpty()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        SimSettings simSettings = SettingsPreferences.getPreferredSimSettings(getApplicationContext());
        SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(SubscriptionManager.class);

        SmsManager manager = SmsManager.getDefault();

        switch (simSettings.getSettings()) {

            case SIM:
                int simIndex = simSettings.getSimIndex();
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

                for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {

                    if (subscriptionInfo.getSimSlotIndex() != SubscriptionManager.INVALID_SIM_SLOT_INDEX && subscriptionInfo.getSimSlotIndex() == simIndex) {
                        manager = SmsManager.getSmsManagerForSubscriptionId(subscriptionInfo.getSubscriptionId());
                        break;
                    }
                }
            case PHONE_SETTINGS:
            case RECEIVING_SIM:
            default:
                manager = SmsManager.getDefault();
        }

        manager.sendTextMessage(
                phone,
                null,
                message,
                null,
                null
        );


    }
}
