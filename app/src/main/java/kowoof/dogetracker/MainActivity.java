package kowoof.dogetracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.haha.perflib.Main;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Currency;
import java.util.Locale;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import kowoof.dogetracker.wallet_memory;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class MainActivity extends DrawerActivity {

    private static int defaultTextColor = Color.rgb(0x75, 0x75, 0x75);
    private SharedPreferences spref;

    //We create doge_rates object and handler to make getting exchange rates wow
    private doge_rates dogeRatesObject;
    private Handler getRatesHandler = new Handler();
    private Handler balanceHandler = new Handler();
    private TextView dogeRatesTextView, hourChangeTextView, dailyChangeTextView,
            weeklyChangeTextView, marketCapTextView, volumeTextView,
            totalSupplyTextView, lastUpdateTextView, allWalletsBalanceTextView;
    private wallet_memory walletMemoryObject;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(MainActivity.this);
        spref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        getTextViews();

        //We create handler with WeakReference to wait for get exchange rates
        balanceHandler = new BalanceHandler(this);
        getRatesHandler = new GetRatesHandler(this);
        
        walletMemoryObject = new wallet_memory(getApplicationContext(), balanceHandler);
        dogeRatesObject = new doge_rates(this);
        startup_refresh();
        rateAppReminder();
    }
    //we check and apply settings here
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first

        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
        checkTrendColor(dogeRatesObject.hourChangeRate, hourChangeTextView);
        checkTrendColor(dogeRatesObject.dailyChangeRate, dailyChangeTextView);
        checkTrendColor(dogeRatesObject.weeklyChangeRate, weeklyChangeTextView);

        refreshRates();
        dialog.setCancelable(false);
        dialog.setMessage("Getting rates and balances...");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        //todo make sure if it's required now
        getRatesHandler.removeCallbacksAndMessages(null);
        balanceHandler.removeCallbacksAndMessages(null);
    }

    private static class BalanceHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private BalanceHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                int isAnyWalletsAdded = msg.arg1;
                // Handle message code
                if(isAnyWalletsAdded==3){
                    activity.allWalletsBalanceTextView.setText(Float.toString(activity.walletMemoryObject.allWalletsBalance) + " Đ");
                    activity.dialog.dismiss();
                } else if (isAnyWalletsAdded==0){
                    activity.allWalletsBalanceTextView.setText("0" + " Đ");
                    activity.dialog.dismiss();
                }
            }
        }
    }

    private static class GetRatesHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private GetRatesHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                int onlineMode = msg.arg1;
                if(onlineMode==1) activity.dogeRatesObject.getCurrentRefreshTime();
                else if(onlineMode==0){
                    activity.dogeRatesObject.getRecentRefreshTime();
                    activity.makeSnackbar("Connection error. Showing last updated rates.");
                    activity.dialog.dismiss();
                }
                activity.updateRatesInView(); //insert updated rates to layout
                activity.checkTrendColor(activity.dogeRatesObject.hourChangeRate, activity.hourChangeTextView);
                activity.checkTrendColor(activity.dogeRatesObject.dailyChangeRate, activity.dailyChangeTextView);
                activity.checkTrendColor(activity.dogeRatesObject.weeklyChangeRate, activity.weeklyChangeTextView);
            }
        }
    }

    public void startup_refresh(){
        boolean auto_wallets_refresh = spref.getBoolean("wallets_auto_refresh", false);
        if(auto_wallets_refresh && isNetworkAvailable()) {
            //Refresh exchange rates every startup
            dialog.show();
            refreshRates();
            walletMemoryObject.allWalletsBalance = 0;
            walletMemoryObject.getBalances();
        } else {
            dogeRatesObject.readRatesFromOffline();
            updateRatesInView();
            String allWalletsBalanceString = String.valueOf(walletMemoryObject.calculateAllWalletsBalance());
            allWalletsBalanceTextView.setText( allWalletsBalanceString + " Đ");
        }
    }
    public void getTextViews(){
        dogeRatesTextView = findViewById(R.id.doge_rate);
        hourChangeTextView = findViewById(R.id.hour_change);
        dailyChangeTextView = findViewById(R.id.daily_change);
        weeklyChangeTextView = findViewById(R.id.weekly_change);
        marketCapTextView = findViewById(R.id.market_cap);
        volumeTextView = findViewById(R.id.volume);
        totalSupplyTextView = findViewById(R.id.total_supply);
        lastUpdateTextView = findViewById(R.id.last_update);
        allWalletsBalanceTextView = findViewById(R.id.textView8);
    }

    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling getRates from doge_rates class
    //Update_rates - update rates in view
    public void refreshButton(View view) {
            dialog.show();
            refreshRates();
            walletMemoryObject.allWalletsBalance = 0;
            walletMemoryObject.getBalances();
    }
    public void refreshRates(){
        //Getting current dogecoin rates from coinmarketcap
        dogeRatesObject.getRates(getRatesHandler, spref.getString("fiat_list","USD"));
    }
    void updateRatesInView() {
        try {
            dogeRatesObject.saveRatesToOffline();
            dogeRatesObject.makeCommasOnRates(); //we add spaces to total supply, volume and market cap to make it clearly
            String fiatSymbol = dogeRatesObject.getFiatSymbol();
            dogeRatesTextView.setText("1Đ = " + dogeRatesObject.dogeFiatRate + " " + fiatSymbol);
            hourChangeTextView.setText("1h: " + dogeRatesObject.hourChangeRate + "%");
            dailyChangeTextView.setText("24h: " + dogeRatesObject.dailyChangeRate + "%");
            weeklyChangeTextView.setText("7d: " + dogeRatesObject.weeklyChangeRate + "%");
            marketCapTextView.setText("Market cap: " + dogeRatesObject.marketCapRate + " " + fiatSymbol);
            volumeTextView.setText("Volume 24h: " + dogeRatesObject.volumeRate + " " + fiatSymbol);
            totalSupplyTextView.setText("Total supply: " + dogeRatesObject.totalSupplyRate + " Đ");
            lastUpdateTextView.setText("Last update: " + dogeRatesObject.lastRefreshRate);
        } catch(NullPointerException e ){
            dogeRatesTextView.setText("1Đ = " + "Err");
            hourChangeTextView.setText("1h: " + "Err");
            dailyChangeTextView.setText("24h: " + "Err");
            weeklyChangeTextView.setText("7d: " + "Err");
            marketCapTextView.setText("Market cap: " + "Err");
            volumeTextView.setText("Volume 24h: " + "Err");
            totalSupplyTextView.setText("Total supply: " + "Err");
            lastUpdateTextView.setText("Last update: " + "Err");
        }
    }
    //Check if percent rate are collapsing or raising
    public void checkTrendColor(String percentRate, TextView percentRateTextView){
        boolean useColorTrendsSetting = spref.getBoolean("arrow_or_color", false);
        if(percentRate == null){
            percentRateTextView.setTextColor(defaultTextColor);
            return;
        }
        if(useColorTrendsSetting) {
            if (percentRate.contains("-")) percentRateTextView.setTextColor(Color.RED);
            else percentRateTextView.setTextColor(Color.GREEN);
        } else {
            percentRateTextView.setTextColor(defaultTextColor);
        }
    }

    private void rateAppReminder(){
        AppRate.with(this)
                .setInstallDays(0) // default 10, 0 means install day.
                .setLaunchTimes(3) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(true) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
    }
}
