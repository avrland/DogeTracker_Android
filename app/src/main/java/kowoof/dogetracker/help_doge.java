package kowoof.dogetracker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class help_doge extends DrawerActivity {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_doge);
    }


    public void sendEmailToAuthor(View view) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { "marcinpopko@outlook.com" });
        intent.putExtra(Intent.EXTRA_SUBJECT, "DogeTracker app question/suggestion");
        intent.putExtra(Intent.EXTRA_TEXT, "Such wow!");
        startActivity(Intent.createChooser(intent, ""));
    }
}
