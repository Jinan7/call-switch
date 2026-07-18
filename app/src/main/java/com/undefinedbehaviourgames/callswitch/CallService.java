package com.undefinedbehaviourgames.callswitch;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class CallService extends Service {

    public static final Intent newIntent(Context context) {
        return new Intent(context, CallService.class);
    }
    private final CallServiceBinder mCallServiceBinder = new CallServiceBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mCallServiceBinder;
    }

    public class CallServiceBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }
}
