package com.undefinedbehaviourgames.callswitch;

import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

public class UI {

    public static int getColor(Context context, int rId) {
        return ResourcesCompat.getColor(context.getResources(), rId, context.getTheme());
    }
}
