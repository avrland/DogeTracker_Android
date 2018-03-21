package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.WindowManager;
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
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */


public class wallet_balance {
    String balance, success;
    private static String url = "https://dogechain.info/api/v1/address/balance/";

    //We download here json response, leaving a information everything is ready to update view
    public void getWalletBalance(final Context currentContext, final Handler walletBalanceHandler, String address){

        StringRequest request = new StringRequest(url + address, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                parseJsonData(string);
                //We're ready, leave messenge for handler to refresh_rates in view
                //It doesn't matter now what kind of messege we send.
                Message news = new Message();
                news.arg1 = 1;
                walletBalanceHandler.sendMessage(news);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                //If something went wrong, we leave messege with error
            }
        });
        RequestQueue rQueue = Volley.newRequestQueue(currentContext);
        rQueue.add(request);
    }
    //Parse Json exchange rates data
    private void parseJsonData(String jsonString) {
        try {
            JSONObject jsonobject = new JSONObject(jsonString);
            balance      = jsonobject.getString("balance");
            success     = jsonobject.getString("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
