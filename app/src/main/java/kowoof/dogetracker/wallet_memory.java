package kowoof.dogetracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 20.01.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 *
 * We are going to save and read wallet address here to sharedpreferences using simply string converted to JSON.
 *
 * We initialize this class in any Activity by passing current context argument and single handler to get feedback from getBalances(), for example:
 *
 * Context current_context;
 * wallet_memory wallet_memory_handler;
 *     protected void onCreate(Bundle savedInstanceState) {
 *                  //other stuff from beggining here
 *                  wallet_memory_handler = new wallet_memory(getApplicationContext(), handler);
 *     }
 */


public class wallet_memory {
    public enum response { OK, }

    private static final String PREFS_FILE = "wallets_file";
    private static final String KEY_STRING = "WALLET_ADDRESS_STORE";
    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    private String walletJsonString;
    private JSONObject jsonObj = new JSONObject();
    private Context currentContext;

    String WALLET_NAME, WALLET_ADDRESS, WALLET_BALANCE;


    int COUNT = 0, wallets_amount = 0;

    private wallet_balance walletBalanceObject = new wallet_balance(); //object for getting wallet balances
    float allWalletsBalance, currentWalletBalance = 0;


    private Handler balanceReceivedHandler = new Handler();
    Handler externalBalanceGetHandler = new Handler();
    //@SuppressLint("HandlerLeak")
    wallet_memory(Context context) {
        currentContext = context;
    }
    wallet_memory(Context context, final Handler handler){
        currentContext = context;
        externalBalanceGetHandler = handler;
        balanceReceivedHandler = new WalletMemoryHandler(this);
    }


    private static class WalletMemoryHandler extends Handler {
        private final WeakReference<wallet_memory> mActivity;

        private WalletMemoryHandler(wallet_memory activity) {
            mActivity = new WeakReference<>(activity);
        }
        wallet_memory activity;
        @Override
        public void handleMessage(Message msg) {
            activity = mActivity.get();
            if (activity != null) {
                try {
                    //We save here already fetched info
                    activity.WALLET_BALANCE = activity.walletBalanceObject.balance; //get single wallet balance when you get it from json query
                    activity.currentWalletBalance = Float.parseFloat(activity.WALLET_BALANCE);
                    activity.saveToWallet(activity.WALLET_NAME, activity.WALLET_ADDRESS, activity.WALLET_BALANCE, activity.COUNT); //save it to json

                    Message news2 = new Message();
                    news2.arg1 = 2;
                    activity.externalBalanceGetHandler.sendMessage(news2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                activity.allWalletsBalance = activity.allWalletsBalance + activity.currentWalletBalance;
                activity.COUNT++;
                //After we saved new wallet balance, we have two possibilities what to do
                if (activity.COUNT < activity.wallets_amount) {
                    //We still have wallets to update, so we order another update
                    activity.getBalances(); //if there are still wallets to read, get another
                } else {
                    //Finished getting all balances, so we send info we're ready
                    Message news = new Message();
                    news.arg1 = 3;
                    activity.externalBalanceGetHandler.sendMessage(news);
                    activity.COUNT = 0;
                }
            }
        }
    }


    //get all wallets to json object from sharedpreferences into class String
    public String readAllWallets() {
        SharedPreferences settings = currentContext.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        walletJsonString = settings.getString(KEY_STRING, "[]");
        return walletJsonString;
    }

    public void getBalances(){
        try {

            JSONArray new_array = new JSONArray(readAllWallets());
            wallets_amount = new_array.length();
            if(wallets_amount > 0) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(COUNT);
                    WALLET_NAME = jsonObject.getString("title");
                    WALLET_ADDRESS = jsonObject.getString("address");
                    //send get balance query with current address, wait in handler for response
                    walletBalanceObject.getWalletBalance(currentContext, balanceReceivedHandler, WALLET_ADDRESS);

                    Message news = new Message();
                    news.arg1 = 1;
                    externalBalanceGetHandler.sendMessage(news);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Message news = new Message();
                news.arg1 = 0;
                externalBalanceGetHandler.sendMessage(news);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //I don't think so we need it now
    public String read_wallet(int number) {
        SharedPreferences settings = currentContext.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        walletJsonString = settings.getString(KEY_STRING, "my string");
        try {
            JSONArray new_array = new JSONArray(walletJsonString);
            JSONObject jsonObject = new_array.getJSONObject(number);
            WALLET_NAME = jsonObject.getString("title");
            WALLET_ADDRESS = jsonObject.getString("address");
            WALLET_BALANCE = jsonObject.getString("notice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return walletJsonString;
    }
    //add new wallet
    //it uses wallet_name and wallet_address arguments, adds to current wallet list from sharepreferences
    public void addToWalletsWithBalance(String wallet_name, String wallet_address, String wallet_balance) throws JSONException {

        jsonObj = new JSONObject();
        try {
            jsonObj.put("title", wallet_name);
            jsonObj.put("notice", wallet_balance);
            jsonObj.put("address", wallet_address);

        } catch (JSONException e) {

        }
        JSONArray new_array = new JSONArray(readAllWallets());
        new_array.put(jsonObj);
        SharedPreferences settings = currentContext.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //save to specific place in json
    public void saveToWallet(String wallet_name, String wallet_address, String wallet_balance, int position) throws JSONException {
        jsonObj = new JSONObject();
        try {
            jsonObj.put("title", wallet_name);
            jsonObj.put("notice", wallet_balance);
            jsonObj.put("address", wallet_address);

        } catch (JSONException e) {
            jsonObj.put("title", "Error");
            jsonObj.put("notice", "Error");
            jsonObj.put("address", "Error");
        }
        JSONArray new_array = new JSONArray(readAllWallets());
        new_array.put(position, jsonObj);

        SharedPreferences settings = currentContext.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //remove wallet
    public void removeWallet(int wallet_id) throws JSONException {
        JSONArray new_array = new JSONArray(readAllWallets());
        new_array = remove(wallet_id, new_array);

        SharedPreferences settings = currentContext.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_STRING, new_array.toString());
        editor.apply();
    }

    //addition methods to remove_wallet
    private static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }
        return ja;
    }

    private static List<JSONObject> asList(final JSONArray ja) {
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

    float calculateAllWalletsBalance(){
        //prepare all balances float handler
        float total_balance_f = 0, current_wallet_f = 0;
        try {
            JSONArray new_array = new JSONArray(readAllWallets());

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
}