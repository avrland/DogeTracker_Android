package kowoof.dogetracker;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
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

import java.lang.ref.WeakReference;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class settings_activity extends AppCompatActivity {

    private doge_rates dogeRatesObject;
    private Handler getRatesHandler;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity);
        Toolbar toolbar = findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prepareProgressDialog();

        getFragmentManager().beginTransaction()
                .replace(R.id.settings_layout, new GeneralPreferenceFragment())
                .commit();

        //We create dogeRates object and handler to wait for get exchange rates (before we quit settings)
        dogeRatesObject = new doge_rates(this);
        getRatesHandler = new GetRatesHandler(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            orderGettingNewRates();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            orderGettingNewRates();
        }
        return super.onOptionsItemSelected(item);
    }

    private static class GetRatesHandler extends Handler {
        private final WeakReference<settings_activity> mActivity;

        private GetRatesHandler(settings_activity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            settings_activity activity = mActivity.get();
            if (activity != null) {
                if(msg.arg1==1) activity.dogeRatesObject.getCurrentRefreshTime();
                else if(msg.arg1==2){
                    activity.dogeRatesObject.getRecentRefreshTime();
                    Snackbar mySnackbar = Snackbar.make(activity.getWindow().getDecorView(), activity.getString(R.string.connectionErrorText), Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
                activity.dialog.dismiss();
                activity.finish();
            }
        }
    }
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);
        }

    }

    private void orderGettingNewRates(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        dogeRatesObject.getRates(getRatesHandler, sp.getString("fiat_list","USD"));
        dialog.show();
    }
    private void prepareProgressDialog(){
        dialog = new ProgressDialog(settings_activity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.savingSettings));
    }
}
