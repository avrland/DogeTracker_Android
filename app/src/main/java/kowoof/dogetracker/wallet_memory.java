package kowoof.dogetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 20.01.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 *
 * We are going to save and read wallet address here to sharedpreferences using simply string converted to JSON.
 *
 * We initialize this class in any Activity by passing current context argument and single handler to get feedback from get_balances(), for example:
 *
 * Context current_context;
 * wallet_memory wallet_memory_handler;
 *     protected void onCreate(Bundle savedInstanceState) {
 *                  //other stuff from beggining here
 *                  wallet_memory_handler = new wallet_memory(getApplicationContext(), handler);
 *     }
 */

public class wallet_memory {

    private static final String PREFS_FILE = "wallets_file";
    private static final String KEY_STRING = "WALLET_ADDRESS_STORE";
    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    String wallet_string;
    JSONObject jsonObj = new JSONObject();
    Context current_context;

    String WALLET_NAME, WALLET_ADDRESS, WALLET_BALANCE;


    int COUNT = 0, wallets_amount = 0;

    wallet_balance local_wallet_balance_handler = new wallet_balance(); //object for getting wallet balances
    float all_wallets_balance, current_wallet_balance = 0;


    Handler balance_received_feedback = new Handler();

    @SuppressLint("HandlerLeak")
    wallet_memory(Context context, final Handler handler) {
        current_context = context;

        //Plan:
        //1 - received single wallet
        //2 - started to getting another
        //3 - task finished
        balance_received_feedback = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    WALLET_BALANCE = local_wallet_balance_handler.balance; //get single wallet balance when you get it from json query
                    current_wallet_balance = Float.parseFloat(WALLET_BALANCE);
                    save_to_wallet(WALLET_NAME, WALLET_ADDRESS , WALLET_BALANCE, COUNT ); //save it to json
                    Message news = new Message();
                    news.arg1 = 1;
                    handler.sendMessage(news);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                COUNT++;
                all_wallets_balance = all_wallets_balance + current_wallet_balance;
                if(COUNT < wallets_amount){
                    Message news = new Message();
                    news.arg1 = 2;
                    handler.sendMessage(news);
                    get_balances(); //if there are still wallets to read, get another
                } else {
                    Message news = new Message();
                    news.arg1 = 3;
                    handler.sendMessage(news);

                    COUNT = 0; //if no, just fill listview
                }
            }

        };
    }

    //read all wallets to json object from sharedpreferences into class String
    public String read_all_wallets() {
        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        wallet_string = settings.getString(KEY_STRING, "[]");
        return wallet_string;
    }

    public void get_balances(){
        try {
            if(read_all_wallets()=="[]"){
                //todo reaction on empty json
            }
            JSONArray new_array = new JSONArray(read_all_wallets());
            wallets_amount = new_array.length();
            try {
                JSONObject jsonObject = new_array.getJSONObject(COUNT);
                WALLET_NAME = jsonObject.getString("title");
                WALLET_ADDRESS = jsonObject.getString("address");
                //send get balance query with current address, wait in handler for response
                local_wallet_balance_handler.get_wallet_balance(current_context, balance_received_feedback, WALLET_ADDRESS);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //I don't think so we need it now
    public String read_wallet(int number) {
        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        wallet_string = settings.getString(KEY_STRING, "my string");
        try {
            JSONArray new_array = new JSONArray(wallet_string);
            JSONObject jsonObject = new_array.getJSONObject(number);
            WALLET_NAME = jsonObject.getString("title");
            WALLET_ADDRESS = jsonObject.getString("address");
            WALLET_BALANCE = jsonObject.getString("notice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("WE HAVE:", WALLET_NAME + " " + WALLET_ADDRESS + " " + WALLET_BALANCE + " ");
        return wallet_string;
    }
    //add new wallet
    //it uses wallet_name and wallet_address arguments, adds to current wallet list from sharepreferences
    public void add_to_wallets_with_balance(String wallet_name, String wallet_address, String wallet_balance) throws JSONException {

        jsonObj = new JSONObject();
        try {
            jsonObj.put("title", wallet_name);
            jsonObj.put("notice", wallet_balance);
            jsonObj.put("address", wallet_address);

        } catch (JSONException e) {

        }
        JSONArray new_array = new JSONArray(read_all_wallets());
        new_array.put(jsonObj);
        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //save to specific place in json
    public void save_to_wallet(String wallet_name, String wallet_address, String wallet_balance, int position) throws JSONException {
        jsonObj = new JSONObject();
        try {
            jsonObj.put("title", wallet_name);
            jsonObj.put("notice", wallet_balance);
            jsonObj.put("address", wallet_address);

        } catch (JSONException e) {

        }
        JSONArray new_array = new JSONArray(read_all_wallets());
        new_array.put(position, jsonObj);

        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //remove wallet
    public void remove_wallet(int wallet_id) throws JSONException {
        JSONArray new_array = new JSONArray(read_all_wallets());
        new_array = remove(wallet_id, new_array);

        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //addition methods to remove_wallet
    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }
        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

}