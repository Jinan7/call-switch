package com.undefinedbehaviourgames.callswitch;

import static android.content.Context.TELEPHONY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.UUID;

public class CallWorker extends Worker {
    public static final String TAG = "CallWorkerLogger";
    private static final String DATA_NUMBER = "number";

    public static Data newData(String number) {

        return new Data.Builder()
                .putString(DATA_NUMBER, number)
                .build();
    }
    public CallWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        String number = getInputData().getString(DATA_NUMBER);
        Log.d(TAG, "Doing work");
        if (number != null)  {
            answerCall(number);
        }

        return Result.success();
    }

    public void answerCall(String number) {

        TelephonyManager tm = (TelephonyManager) getSystemService(getApplicationContext(), TelephonyManager.class);
        String country = tm.getNetworkCountryIso();
        Log.d(TAG, country);
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneProto = phoneNumberUtil.parse(number, country.toUpperCase());
            Log.d(TAG, phoneProto.toString());
            Contact contact = ContactLab.getInstance(getApplicationContext()).get(phoneProto);

            if (contact != null) {
                Reply reply = ReplyLab.getInstance(getApplicationContext()).get(contact.getActiveReplyId());
                if (reply != null && reply.isEnabled()) {
                    sendMessage(reply.getReply(), contact.getPhone());
                }

            } else {
                UUID id = ContactPreferences.getUnknownContact(getApplicationContext()).getActiveReplyId();
                Reply reply = ReplyLab.getInstance(getApplicationContext()).get(id);
                if (reply != null && reply.isEnabled()) {
                    sendMessage(reply.getReply(), number);
                }
            }
        } catch (NumberParseException e) {
            Log.d(TAG, "could not parse number");
            e.printStackTrace();
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
        SubscriptionManager subscriptionManager = (SubscriptionManager) getSystemService(getApplicationContext(), SubscriptionManager.class);

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
