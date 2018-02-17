package kowoof.dogetracker;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class settings_activity extends AppCompatActivity {

    private doge_rates dogeRatesObject;
    private Handler getRatesHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new GeneralPreferenceFragment())
                .commit();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        dogeRatesObject = new doge_rates(this);
        //We create handler to wait for get exchange rates
        getRatesHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                if(msg.arg1==1)      dogeRatesObject.getCurrentRefreshTime();
                else if(msg.arg1==2){
                    dogeRatesObject.getRecentRefreshTime();
                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Connection error. Showing last updated rates.", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
                finish();
            }

        };
    }
    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            dogeRatesObject.getRates(getRatesHandler, sp.getString("fiat_list","USD"));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            dogeRatesObject.getRates(getRatesHandler, sp.getString("fiat_list","USD"));
        }
        return super.onOptionsItemSelected(item);
    }


    public static class GeneralPreferenceFragment extends PreferenceFragment {


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }

    }
}
