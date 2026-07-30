package com.undefinedbehaviourgames.callswitch;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class BottomNavBarFragment extends Fragment {

    private LinearLayout mBottomNavigationView;
    protected void setUpNavBar(View v, int exclude) {
        mBottomNavigationView = v.findViewById(R.id.bottom_nav_view_alt);
        ViewCompat.setOnApplyWindowInsetsListener(mBottomNavigationView, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        updateUI(v.findViewById(R.id.nav_contacts), v.findViewById(R.id.nav_contacts_icon), exclude == R.id.nav_contacts);
        updateUI(v.findViewById(R.id.nav_replies), v.findViewById(R.id.nav_replies_icon), exclude == R.id.nav_replies);

        wire(v.findViewById(R.id.reply_menu_button), R.id.nav_replies, exclude, RepliesActivity.newIntent(getContext()));
        wire(v.findViewById(R.id.contact_menu_button), R.id.nav_contacts, exclude, ContactsActivity.newIntent(getContext()));
    }

   private void updateUI(FrameLayout backgroundLayout, ImageView iconLayout, boolean selected) {



        if (selected) {
            GradientDrawable background = (GradientDrawable) backgroundLayout.getBackground();
            background.mutate();
            background.setColor(UI.getColor(getContext(), R.color.white));
            Drawable drawable = iconLayout.getDrawable();
            drawable.mutate();
            drawable.setTint(UI.getColor(getContext(), R.color.blue));
            iconLayout.setImageDrawable(drawable);
        } else {
            GradientDrawable background = (GradientDrawable) backgroundLayout.getBackground();
            background.mutate();
            background.setColor(UI.getColor(getContext(), R.color.blue));
            Drawable drawable = iconLayout.getDrawable();
            drawable.mutate();
            drawable.setTint(UI.getColor(getContext(), R.color.white));
            iconLayout.setImageDrawable(drawable);
        }
   }

   private void wire(View v, int viewId, int exclude, Intent intent) {

        if (viewId == exclude) return;

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                getActivity().overridePendingTransition(0, 0);
                if (!(getActivity() instanceof RepliesActivity)) {
                    getActivity().finish();
                }
            }
        });
   }

}
