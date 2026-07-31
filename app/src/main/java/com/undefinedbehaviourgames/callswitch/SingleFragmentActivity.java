package com.undefinedbehaviourgames.callswitch;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class SingleFragmentActivity extends AppCompatActivity {

    private static final String TAG = "SingleFragmentActivityLogger";
    private ExecutorService mExecutor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_fragment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, 0);
            return insets;
        });

        //make status bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(false);

        //make navigation bar icons and text light color
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightNavigationBars(false);

        //make nav system bar transparent or make is same color
        //as nav bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            getWindow().setNavigationBarContrastEnforced(false);
        } else {
            getWindow().setNavigationBarColor(getColor(R.color.blue));
        }

        FragmentManager fm = getSupportFragmentManager();

        Fragment frag = fm.findFragmentById(R.id.main);

        if (frag == null) {
            frag = createFragment();
            fm.beginTransaction().add(R.id.main, frag).commit();
        }

        mExecutor = Executors.newSingleThreadExecutor();

        if (ContactQueryHandler.getInstance(this).getQueryState() == State.IDLE) {
            mExecutor.submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    ContactQueryHandler.getInstance(SingleFragmentActivity.this).startQuery(null);
                    return null;
                }
            });
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutor.shutdownNow();
    }

    public abstract Fragment createFragment();
}
