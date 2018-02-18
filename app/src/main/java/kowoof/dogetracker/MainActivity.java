package kowoof.dogetracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Currency;
import java.util.Locale;

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

        //We create handler to wait for get exchange rates
        getRatesHandler = new Handler(new GetRatesCallback());
        balanceHandler = new Handler(new BalanceHandlerCallback());

        walletMemoryObject = new wallet_memory(getApplicationContext(), balanceHandler);
        dogeRatesObject = new doge_rates(this);
        startup_refresh();

    }
    //we check and apply settings here
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
            allWalletsBalanceTextView.setText(Float.toString(calculateAllWalletsBalance()) + " Đ");
        }

    }
    float calculateAllWalletsBalance(){
        //prepare all balances float handler
        float total_balance_f = 0, current_wallet_f = 0;
        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.readAllWallets());

            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    try {
                        current_wallet_f = Float.parseFloat(jsonObject.getString("notice"));
                    } catch (NumberFormatException e) {
                        current_wallet_f = 0;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                total_balance_f += current_wallet_f;
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return total_balance_f;
    }

    class BalanceHandlerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            int isAnyWalletsAdded = message.arg1;
            // Handle message code
            if(isAnyWalletsAdded==3){
                allWalletsBalanceTextView.setText(Float.toString(walletMemoryObject.allWalletsBalance) + " Đ");
                dialog.dismiss();
            } else if (isAnyWalletsAdded==0){
                allWalletsBalanceTextView.setText("0" + " Đ");
                dialog.dismiss();
            }
            return true;
        }
    }
    class GetRatesCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message message) {
            int onlineMode = message.arg1;
            if(onlineMode==1) dogeRatesObject.getCurrentRefreshTime();
            else if(onlineMode==0){
                dogeRatesObject.getRecentRefreshTime();
                makeSnackbar("Connection error. Showing last updated rates.");
                dialog.dismiss();
            }
            updateRatesInView(); //insert updated rates to layout
            checkTrendColor(dogeRatesObject.hourChangeRate, hourChangeTextView);
            checkTrendColor(dogeRatesObject.dailyChangeRate, dailyChangeTextView);
            checkTrendColor(dogeRatesObject.weeklyChangeRate, weeklyChangeTextView);
            return true;
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


    public void onStop(){
        super.onStop();
        getRatesHandler.removeCallbacksAndMessages(null);
        balanceHandler.removeCallbacksAndMessages(null);
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
            String fiatSymbol = getFiatSymbol();
            dogeRatesObject.saveRatesToOffline();
            dogeRatesObject.makeCommasOnRates(); //we add spaces to total supply, volume and market cap to make it clearly
            dogeRatesTextView.setText("1Đ = " + dogeRatesObject.dogeFiatRate + " " + fiatSymbol);
            hourChangeTextView.setText("1h: " + dogeRatesObject.hourChangeRate + "%");
            dailyChangeTextView.setText("24h: " + dogeRatesObject.dailyChangeRate + "%");
            weeklyChangeTextView.setText("7d: " + dogeRatesObject.weeklyChangeRate + "%");
            marketCapTextView.setText("Market cap: " + dogeRatesObject.marketCapRate + " " + fiatSymbol);
            volumeTextView.setText("Volume 24h: " + dogeRatesObject.volumeRate + " " + fiatSymbol);
            totalSupplyTextView.setText("Total supply: " + dogeRatesObject.totalSupplyRate + " Đ");
            lastUpdateTextView.setText("Last update: " + dogeRatesObject.lastRefreshRate);
        } catch(NullPointerException e ){

        }
    }
    public String getFiatSymbol(){
        String fiat_code = spref.getString("fiat_list", "USD");
        Locale.setDefault(new Locale("lv", "LV"));
        Currency used_fiat_currency = Currency.getInstance(fiat_code);
        return used_fiat_currency.getSymbol();
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
}
