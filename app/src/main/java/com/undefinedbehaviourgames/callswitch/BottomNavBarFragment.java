package com.undefinedbehaviourgames.callswitch;

import android.content.Intent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class BottomNavBarFragment extends Fragment {

    private BottomNavigationView mBottomNavigationView;
    protected void setUpNavBar(View v, int exclude) {
        mBottomNavigationView = v.findViewById(R.id.bottom_nav_view);

        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                if (menuItem.getItemId() == exclude) return false;
                if (menuItem.getItemId() == R.id.menu_contacts) {
                    Intent intent = ContactsActivity.newIntent(getContext());
                    startActivity(intent);
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_replies) {
                    Intent intent = RepliesActivity.newIntent(getContext());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    protected void setUpNavBar(View v) {
        mBottomNavigationView = v.findViewById(R.id.bottom_nav_view);

        mBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_contacts) {
                    Intent intent = ContactsActivity.newIntent(getContext());
                    startActivity(intent);
                    return true;
                } else if (menuItem.getItemId() == R.id.menu_replies) {
                    Intent intent = RepliesActivity.newIntent(getContext());
                    startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }
}
