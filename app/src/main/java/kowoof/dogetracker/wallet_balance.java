package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Marcin on 14.01.2018.
 */

public class wallet_balance {
    String balance, success;
    private ProgressDialog dialog;
    private static String url = "https://dogechain.info/api/v1/address/balance/";

    //We download here json response, leaving a information everything is ready to update view
    public void get_wallet_balance(final Context current_context, final Handler do_it_now, String address){
        dialog = new ProgressDialog(current_context);
        dialog.setMessage("Loading....");
        dialog.show();

        StringRequest request = new StringRequest(url + address, new Response.Listener<String>() {
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
            JSONObject jsonobject = new JSONObject(jsonString);
            balance      = jsonobject.getString("balance");
            success     = jsonobject.getString("success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
