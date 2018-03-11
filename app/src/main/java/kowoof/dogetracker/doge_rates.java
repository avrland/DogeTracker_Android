package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;

/**
 * Created by Marcin on 10.01.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 *
 * We get here exchange rates via coinmarketcap and save it to sharepreferences also.
 */

public class doge_rates {
        String dogeFiatRate, hourChangeRate, dailyChangeRate, weeklyChangeRate, marketCapRate, volumeRate,
                totalSupplyRate, lastRefreshRate;
        private static ProgressDialog DIALOG;
        private static String URL = "https://api.coinmarketcap.com/v1/ticker/dogecoin/";
        private Context CURRENT_CONTEXT;

        //we store exchange rates stuff into memory
        private static final String PREFS_FILE = "Offline_exchange_rates";
        private static final int PREFS_MODE = Context.MODE_PRIVATE;
        private static final String dogeFiatRateOffline = "doge_rate_offline";
        private static final String hourChangeRateOffline = "hour_change_offline";
        private static final String dailyChangeRateOffline = "daily_change_offline";
        private static final String weeklyChangeRateOffline = "weekly_change_offline";
        private static final String marketCapRateOffline = "market_cap_offline";
        private static final String volumeRateOffline = "volume_offline";
        private static final String totalSupplyRateOffline = "total_supply_offline";
        private static final String lastRefreshOffline = "last_refresh_offline";

        doge_rates(Context user_context){
            CURRENT_CONTEXT = user_context;
        }

        //We download here json response, leaving a information everything is ready to update view
        public void getRates(final Handler handler, final String fiatCurrency){
            DIALOG = new ProgressDialog(CURRENT_CONTEXT);

            StringRequest request = new StringRequest(URL + "?convert=" + fiatCurrency.toLowerCase(), new Response.Listener<String>() {
                @Override
                public void onResponse(String string) {
                    parseJsonData(string, fiatCurrency.toLowerCase());
                    //We're ready, leave messenge for handler to refresh_rates in view
                    //It doesn't matter now what kind of messege we send.
                    Message news = new Message();
                    news.arg1 = 1;
                    handler.sendMessage(news);
                    saveRatesToOffline();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Message news = new Message();
                    news.arg1 = 0;
                    handler.sendMessage(news);
                    readRatesFromOffline();
                }
            });
            RequestQueue rQueue = Volley.newRequestQueue(CURRENT_CONTEXT);
            rQueue.add(request);
        }
        //Parse Json exchange rates data
        private void parseJsonData(String jsonString, String fiat_currency) {
        try {
            JSONArray jsonarray = new JSONArray(jsonString);
            for(int i=0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                dogeFiatRate       = jsonobject.getString("price_" + fiat_currency);
                //we want 4 decimal places with dot as a separator
                dogeFiatRate = cutDecimalPlacesToFour(dogeFiatRate);
                hourChangeRate     = jsonobject.getString("percent_change_1h");
                dailyChangeRate    = jsonobject.getString("percent_change_24h");
                weeklyChangeRate   = jsonobject.getString("percent_change_7d");
                marketCapRate      = jsonobject.getString("market_cap_" + fiat_currency);
                volumeRate          = jsonobject.getString("24h_volume_" + fiat_currency);
                totalSupplyRate    = jsonobject.getString("total_supply");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        }
        private String cutDecimalPlacesToFour(String FiatRate){
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            DecimalFormat df = new DecimalFormat("#.####");
            symbols.setDecimalSeparator('.');
            df.setDecimalFormatSymbols(symbols);
            FiatRate = df.format(Float.parseFloat(FiatRate));
            return FiatRate;
        }

        public void readRatesFromOffline(){
            SharedPreferences rates = CURRENT_CONTEXT.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            dogeFiatRate       = rates.getString(dogeFiatRateOffline, "doge_rate_offline");
            hourChangeRate     = rates.getString(hourChangeRateOffline, "hour_change_offline");
            dailyChangeRate    = rates.getString(dailyChangeRateOffline, "daily_change_offline");
            weeklyChangeRate   = rates.getString(weeklyChangeRateOffline, "weekly_change_offline");
            marketCapRate      = rates.getString(marketCapRateOffline, "market_cap_offline");
            volumeRate          = rates.getString(volumeRateOffline, "volume_offline");
            totalSupplyRate    = rates.getString(totalSupplyRateOffline, "total_supply_offline");
            lastRefreshRate    = rates.getString(lastRefreshOffline, "last_refresh_offline");
        }
        public void saveRatesToOffline(){
            SharedPreferences rates = CURRENT_CONTEXT.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = rates.edit();
            editor.putString(dogeFiatRateOffline, dogeFiatRate);
            editor.putString(hourChangeRateOffline, hourChangeRate);
            editor.putString(dailyChangeRateOffline, dailyChangeRate);
            editor.putString(weeklyChangeRateOffline, weeklyChangeRate);
            editor.putString(marketCapRateOffline, marketCapRate);
            editor.putString(volumeRateOffline, volumeRate);
            editor.putString(totalSupplyRateOffline, totalSupplyRate);
            editor.apply();
        }
        public void getCurrentRefreshTime(){
            SharedPreferences rates = CURRENT_CONTEXT.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = rates.edit();
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            lastRefreshRate =df.format(Calendar.getInstance().getTime());
            editor.putString(lastRefreshOffline, lastRefreshRate);
            editor.apply();
        }
        public void getRecentRefreshTime(){
            SharedPreferences rates = CURRENT_CONTEXT.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            lastRefreshRate    = rates.getString(lastRefreshOffline, "last_refresh_offline");
        }
        //we add spaces to so big numbers like market cap, volume and total supply
        public void makeCommasOnRates(){
            try {
                DecimalFormat decimalFormat1 = new DecimalFormat("#,###");
                float market_cap_f = Float.parseFloat(marketCapRate);
                marketCapRate = decimalFormat1.format(market_cap_f);

                float volume_f = Float.parseFloat(volumeRate);
                volumeRate = decimalFormat1.format(volume_f);

                float total_supply_f = Float.parseFloat(totalSupplyRate);
                totalSupplyRate = decimalFormat1.format(total_supply_f);
            } catch(NumberFormatException e ){
                marketCapRate = "0";
                volumeRate = "0";
                totalSupplyRate = "0";
            }
        }

    public float getDogeFiatRate(){
        readRatesFromOffline();
        float fiatDogeFloat = Float.parseFloat(dogeFiatRate);
        return fiatDogeFloat;
    }
    public String getFiatSymbol(){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CURRENT_CONTEXT);
        String fiatCode = sp.getString("fiat_list","USD");
        Currency usedFiatCurrency  = Currency.getInstance(fiatCode);
        return usedFiatCurrency.getSymbol();
    }
}
