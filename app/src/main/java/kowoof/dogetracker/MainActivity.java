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

    private static int default_text_color = Color.rgb(0x75, 0x75, 0x75);
    private SharedPreferences spref;

    //We create doge_rates object and handler to make getting exchange rates wow
    private doge_rates current_doge_rates;
    private Handler get_rates_handler = new Handler();
    private TextView doge_rates_text, hour_change_text, daily_change_text,
            weekly_change_text, market_cap_text, volume_text,
            total_supply_text, last_update_text;
    private wallet_memory wallet_memory_handler;
    private ProgressDialog dialog;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(MainActivity.this);
        spref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

        doge_rates_text = findViewById(R.id.doge_rate);
        hour_change_text = findViewById(R.id.hour_change);
        daily_change_text = findViewById(R.id.daily_change);
        weekly_change_text = findViewById(R.id.weekly_change);
        market_cap_text = findViewById(R.id.market_cap);
        volume_text = findViewById(R.id.volume);
        total_supply_text = findViewById(R.id.total_supply);
        last_update_text = findViewById(R.id.last_update);

        //We create handler to wait for get exchange rates
        get_rates_handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1==1)      current_doge_rates.get_new_refresh_time();
                else if(msg.arg1==2){
                    current_doge_rates.get_last_refresh_time();
                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Connection error. Showing last updated rates.", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
                update_rates_in_view(); //insert updated rates to layout
                check_color_trend(current_doge_rates.hour_change, hour_change_text);
                check_color_trend(current_doge_rates.daily_change, daily_change_text);
                check_color_trend(current_doge_rates.weekly_change, weekly_change_text);
            }

        };
        Handler balance_handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.arg1==3){
                    TextView all_wallets_balance = findViewById(R.id.textView8);
                    all_wallets_balance.setText(Float.toString(wallet_memory_handler.all_wallets_balance) + " Đ");
                    dialog.dismiss();
                }
            }

        };
        wallet_memory_handler = new wallet_memory(getApplicationContext(), balance_handler);
        current_doge_rates = new doge_rates(this);


        //Refresh exchange rates every startup
        dialog.setMessage("Loading....");
        dialog.show();
        wallet_memory_handler.all_wallets_balance = 0;
        wallet_memory_handler.get_balances();
    }

    //we check and apply settings here
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        boolean use_background_logo_setting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!use_background_logo_setting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
        check_color_trend(current_doge_rates.hour_change, hour_change_text);
        check_color_trend(current_doge_rates.daily_change, daily_change_text);
        check_color_trend(current_doge_rates.weekly_change, weekly_change_text);
        refresh_rates();
    }


    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling get_rates from doge_rates class
    //Update_rates - update rates in view
    public void refresh_button(View view) {
            refresh_rates();
            dialog.show();
            wallet_memory_handler.all_wallets_balance = 0;
            wallet_memory_handler.get_balances();
    }
    public void refresh_rates(){
        //Getting current dogecoin rates from coinmarketcap
        current_doge_rates.get_rates(get_rates_handler, spref.getString("fiat_list","USD"));
    }
    void update_rates_in_view() {
        try {
            String fiat_code = spref.getString("fiat_list", "USD");
            Locale.setDefault(new Locale("lv", "LV"));
            Currency used_fiat_currency = Currency.getInstance(fiat_code);
            current_doge_rates.save_rates_to_offline();
            current_doge_rates.rates_with_commas(); //we add spaces to total supply, volume and market cap to make it clearly
            doge_rates_text.setText("1Đ = " + current_doge_rates.doge_rate + " " + used_fiat_currency.getSymbol());
            hour_change_text.setText("1h: " + current_doge_rates.hour_change + "%");
            daily_change_text.setText("24h: " + current_doge_rates.daily_change + "%");
            weekly_change_text.setText("7d: " + current_doge_rates.weekly_change + "%");
            market_cap_text.setText("Market cap: " + current_doge_rates.market_cap + " " + used_fiat_currency.getSymbol());
            volume_text.setText("Volume 24h: " + current_doge_rates.volume + " " + used_fiat_currency.getSymbol());
            total_supply_text.setText("Total supply: " + current_doge_rates.total_supply + " Đ");
            last_update_text.setText("Last update: " + current_doge_rates.last_refresh);
        } catch(NullPointerException e ){

        }
    }
    //Check if percent rate are collapsing or raising
    public void check_color_trend(String percent_rate, TextView percent_rate_textview){
        boolean use_color_trends_setting = spref.getBoolean("arrow_or_color", false);
        if(percent_rate == null){
            percent_rate_textview.setTextColor(default_text_color);
            return;
        }
        if(use_color_trends_setting) {
            if (percent_rate.contains("-")) percent_rate_textview.setTextColor(Color.RED);
            else percent_rate_textview.setTextColor(Color.GREEN);
        } else {
            percent_rate_textview.setTextColor(default_text_color);
        }
    }
}
