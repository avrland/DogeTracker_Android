package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
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

/**
 * Created by Marcin on 10.01.2018.
 */

public class doge_rates {
        String doge_rate, hour_change, daily_change, weekly_change, market_cap, volume,
                total_supply;
        private ProgressDialog dialog;
        private String url = "https://api.coinmarketcap.com/v1/ticker/dogecoin/";

        //We download here json response, leaving a information everything is ready to update view
        public void get_rates(final Context current_context, final Handler do_it_now){
            dialog = new ProgressDialog(current_context);
            dialog.setMessage("Loading....");
            dialog.show();

            StringRequest request = new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String string) {
                    parseJsonData(string);
                    dialog.dismiss();
                    //We're ready, leave messenge for handler to refresh_rates in view
                    //It doesn't matter now what kind of messege we send.
                    Message news = new Message();
                    news.arg1 = 1;
                    do_it_now.sendMessage(news);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //If something went wrong, we leave messege with error
                    Toast.makeText(current_context, "Connection error.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
            RequestQueue rQueue = Volley.newRequestQueue(current_context);
            rQueue.add(request);
        }
        //Parse Json exchange rates data
        private void parseJsonData(String jsonString) {
        try {
            JSONArray jsonarray = new JSONArray(jsonString);
            for(int i=0; i < jsonarray.length(); i++) {
                JSONObject jsonobject = jsonarray.getJSONObject(i);
                doge_rate       = jsonobject.getString("price_usd");
                hour_change     = jsonobject.getString("percent_change_1h");
                daily_change    = jsonobject.getString("percent_change_24h");
                weekly_change   = jsonobject.getString("percent_change_7d");
                market_cap      = jsonobject.getString("market_cap_usd");
                volume          = jsonobject.getString("24h_volume_usd");
                total_supply    = jsonobject.getString("total_supply");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        }

}
