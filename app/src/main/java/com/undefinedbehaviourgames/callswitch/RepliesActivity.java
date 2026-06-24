package com.undefinedbehaviourgames.callswitch;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class RepliesActivity extends SingleFragmentActivity {

    @Override
    public Fragment createFragment() {
        return RepliesFragment.newInstance();
    }
}