package com.undefinedbehaviourgames.callswitch;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

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
            number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                Log.d(TAG, "Incoming call from " + number);
                Intent callServiceIntent = CallService.newIntent(context);
                context.bindService(callServiceIntent, mCallServiceConnection, Context.BIND_AUTO_CREATE);
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


