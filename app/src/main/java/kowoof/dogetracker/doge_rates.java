package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
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

/**
 * Created by Marcin on 10.01.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 *
 * We get here exchange rates via coinmarketcap and save it to sharepreferences also.
 */

public class doge_rates {
        String doge_rate, hour_change, daily_change, weekly_change, market_cap, volume,
                total_supply, last_refresh;
        private ProgressDialog dialog;
        private static String url = "https://api.coinmarketcap.com/v1/ticker/dogecoin/";
        private Context current_context;

        //we store exchange rates stuff into memory
        private static final String PREFS_FILE = "Offline_exchange_rates";
        private static final int PREFS_MODE = Context.MODE_PRIVATE;
        private static final String doge_rate_offline = "doge_rate_offline";
        private static final String hour_change_offline = "hour_change_offline";
        private static final String daily_change_offline = "daily_change_offline";
        private static final String weekly_change_offline = "weekly_change_offline";
        private static final String market_cap_offline = "market_cap_offline";
        private static final String volume_offline = "volume_offline";
        private static final String total_supply_offline = "total_supply_offline";
        private static final String last_refresh_offline = "last_refresh_offline";

        doge_rates(Context user_context){
                current_context = user_context;
        }

        //We download here json response, leaving a information everything is ready to update view
        public void get_rates(final Handler handler, final String fiat_currency){
            dialog = new ProgressDialog(current_context);
            dialog.setMessage("Loading....");
            dialog.show();

            Log.e("fiat_currency:", fiat_currency);
            StringRequest request = new StringRequest(url + "?convert=" + fiat_currency.toLowerCase(), new Response.Listener<String>() {
                @Override
                public void onResponse(String string) {
                    parseJsonData(string, fiat_currency.toLowerCase());
                    dialog.dismiss();
                    //We're ready, leave messenge for handler to refresh_rates in view
                    //It doesn't matter now what kind of messege we send.
                    Message news = new Message();
                    news.arg1 = 1;
                    handler.sendMessage(news);
                    save_rates_to_offline();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //If something went wrong, we leave messege with error
//                    Toast.makeText(current_context, "Connection error. Showing last updated rates.", Toast.LENGTH_SHORT).show();
                    Message news = new Message();
                    news.arg1 = 2;
                    handler.sendMessage(news);
                    read_rates_from_offline();
                    dialog.dismiss();
                }
            });
            RequestQueue rQueue = Volley.newRequestQueue(current_context);
            rQueue.add(request);
        }
        //Parse Json exchange rates data
        private void parseJsonData(String jsonString, String fiat_currency) {
        try {
            JSONArray jsonarray = new JSONArray(jsonString);
            for(int i=0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                doge_rate       = jsonobject.getString("price_" + fiat_currency);
                //we want 4 decimal places with dot as a separator
                DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                DecimalFormat df = new DecimalFormat("#.####");
                symbols.setDecimalSeparator('.');
                df.setDecimalFormatSymbols(symbols);
                doge_rate = df.format(Float.parseFloat(doge_rate)).toString();
                hour_change     = jsonobject.getString("percent_change_1h");
                daily_change    = jsonobject.getString("percent_change_24h");
                weekly_change   = jsonobject.getString("percent_change_7d");
                market_cap      = jsonobject.getString("market_cap_" + fiat_currency);
                volume          = jsonobject.getString("24h_volume_" + fiat_currency);
                total_supply    = jsonobject.getString("total_supply");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        }
        //We read here rates from offline
        public void read_rates_from_offline(){
            SharedPreferences rates = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            doge_rate       = rates.getString(doge_rate_offline, "doge_rate_offline");
            hour_change     = rates.getString(hour_change_offline, "hour_change_offline");
            daily_change    = rates.getString(daily_change_offline, "daily_change_offline");
            weekly_change   = rates.getString(weekly_change_offline, "weekly_change_offline");
            market_cap      = rates.getString(market_cap_offline, "market_cap_offline");
            volume          = rates.getString(volume_offline, "volume_offline");
            total_supply    = rates.getString(total_supply_offline, "total_supply_offline");
            last_refresh    = rates.getString(last_refresh_offline, "last_refresh_offline");
        }
        //We save here rates to offline
        public void save_rates_to_offline(){
            SharedPreferences rates = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = rates.edit();
            editor.putString(doge_rate_offline, doge_rate);
            editor.putString(hour_change_offline, hour_change);
            editor.putString(daily_change_offline, daily_change);
            editor.putString(weekly_change_offline, weekly_change);
            editor.putString(market_cap_offline, market_cap);
            editor.putString(volume_offline, volume);
            editor.putString(total_supply_offline, total_supply);
            editor.apply();
        }
        public void new_refresh_time(){
            SharedPreferences rates = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            SharedPreferences.Editor editor = rates.edit();
            DateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
            last_refresh =df.format(Calendar.getInstance().getTime());
            editor.putString(last_refresh_offline, last_refresh);
            editor.apply();
        }
        public void offline_refresh_time(){
            SharedPreferences rates = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
            last_refresh    = rates.getString(last_refresh_offline, "last_refresh_offline");
        }
        //we add spaces to so big numbers like market cap, volume and total supply
        public void rates_with_commas(){
            try {
                DecimalFormat decimalFormat1 = new DecimalFormat("#,###");
                float market_cap_f = Float.parseFloat(market_cap);
                market_cap = decimalFormat1.format(market_cap_f);

                float volume_f = Float.parseFloat(volume);
                volume = decimalFormat1.format(volume_f);

                float total_supply_f = Float.parseFloat(total_supply);
                total_supply = decimalFormat1.format(total_supply_f);
            } catch(NumberFormatException e ){

            }
        }

}
