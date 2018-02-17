package kowoof.dogetracker;

import android.annotation.SuppressLint;
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
    private TextView dogeRatesTextView, hourChangeTextView, dailyChangeTextView,
            weeklyChangeTextView, marketCapTextView, volumeTextView,
            totalSupplyTextView, lastUpdateTextView;
    private wallet_memory walletMemoryObject;
    private ProgressDialog dialog;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(MainActivity.this);
        spref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        dogeRatesTextView = findViewById(R.id.doge_rate);
        hourChangeTextView = findViewById(R.id.hour_change);
        dailyChangeTextView = findViewById(R.id.daily_change);
        weeklyChangeTextView = findViewById(R.id.weekly_change);
        marketCapTextView = findViewById(R.id.market_cap);
        volumeTextView = findViewById(R.id.volume);
        totalSupplyTextView = findViewById(R.id.total_supply);
        lastUpdateTextView = findViewById(R.id.last_update);

        //We create handler to wait for get exchange rates
        getRatesHandler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1==1) dogeRatesObject.get_new_refresh_time();
                else if(msg.arg1==2){
                    dogeRatesObject.get_last_refresh_time();
                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Connection error. Showing last updated rates.", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
                updateRatesInView(); //insert updated rates to layout
                checkTrendColor(dogeRatesObject.hour_change, hourChangeTextView);
                checkTrendColor(dogeRatesObject.daily_change, dailyChangeTextView);
                checkTrendColor(dogeRatesObject.weekly_change, weeklyChangeTextView);
            }

        };
        Handler balance_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1==3){
                    TextView allWalletsBalanceTextView = findViewById(R.id.textView8);
                    allWalletsBalanceTextView.setText(Float.toString(walletMemoryObject.all_wallets_balance) + " Đ");
                    dialog.dismiss();
                }
            }

        };
        walletMemoryObject = new wallet_memory(getApplicationContext(), balance_handler);
        dogeRatesObject = new doge_rates(this);


        //Refresh exchange rates every startup
        dialog.setMessage("Loading....");
        dialog.show();
        walletMemoryObject.all_wallets_balance = 0;
        walletMemoryObject.getBalances();
    }

    //we check and apply settings here
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
        checkTrendColor(dogeRatesObject.hour_change, hourChangeTextView);
        checkTrendColor(dogeRatesObject.daily_change, dailyChangeTextView);
        checkTrendColor(dogeRatesObject.weekly_change, weeklyChangeTextView);
        refreshRates();
    }


    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling get_rates from doge_rates class
    //Update_rates - update rates in view
    public void refreshButton(View view) {
            refreshRates();
            dialog.show();
            walletMemoryObject.all_wallets_balance = 0;
            walletMemoryObject.getBalances();
    }
    public void refreshRates(){
        //Getting current dogecoin rates from coinmarketcap
        dogeRatesObject.get_rates(getRatesHandler, spref.getString("fiat_list","USD"));
    }
    void updateRatesInView() {
        try {
            String fiatSymbol = getFiatSymbol();
            dogeRatesObject.save_rates_to_offline();
            dogeRatesObject.rates_with_commas(); //we add spaces to total supply, volume and market cap to make it clearly
            dogeRatesTextView.setText("1Đ = " + dogeRatesObject.doge_rate + " " + fiatSymbol);
            hourChangeTextView.setText("1h: " + dogeRatesObject.hour_change + "%");
            dailyChangeTextView.setText("24h: " + dogeRatesObject.daily_change + "%");
            weeklyChangeTextView.setText("7d: " + dogeRatesObject.weekly_change + "%");
            marketCapTextView.setText("Market cap: " + dogeRatesObject.market_cap + " " + fiatSymbol);
            volumeTextView.setText("Volume 24h: " + dogeRatesObject.volume + " " + fiatSymbol);
            totalSupplyTextView.setText("Total supply: " + dogeRatesObject.total_supply + " Đ");
            lastUpdateTextView.setText("Last update: " + dogeRatesObject.last_refresh);
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
