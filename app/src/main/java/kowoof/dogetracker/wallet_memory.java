package kowoof.dogetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marcin on 20.01.2018.
 *
 * We are going to save and read wallet address here to sharedpreferences using simply string converted to JSON.
 *
 * We initialize this class in any Activity by passing current context argument, for example:
 *
 * Context current_context;
 * wallet_memory wallet_memory_handler;
 *     protected void onCreate(Bundle savedInstanceState) {
 *                  //other stuff from beggining here
 *                  wallet_memory_handler = new wallet_memory(getApplicationContext());
 *     }
 */

public class wallet_memory {
    private static final String PREFS_FILE = "wallets_file";
    private static final String KEY_STRING = "WALLET_ADDRESS_STORE";
    // PREFS_MODE defines which apps can access the file
    private static final int PREFS_MODE = Context.MODE_PRIVATE;
    String wallet_string;
    JSONObject jsonObj = new JSONObject();
    Context current_context;

    String wallet_name, wallet_address, wallet_balance;

    wallet_memory(Context context){
        current_context = context;
    }
    //read all wallets to json object from sharedpreferences into class String
    public String read_all_wallets(){
        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        wallet_string = settings.getString(KEY_STRING, " ");
        return wallet_string;
    }
    //I don't think so we need it now
    public String read_wallet(int number){
        SharedPreferences settings = current_context.getSharedPreferences(PREFS_FILE, PREFS_MODE);
        wallet_string = settings.getString(KEY_STRING, "my string");
        try {
            JSONArray new_array = new JSONArray(wallet_string);
            JSONObject jsonObject = new_array.getJSONObject(number);
            wallet_name = jsonObject.getString("title");
            wallet_address = jsonObject.getString("address");
            wallet_balance = jsonObject.getString("notice");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.e("WE HAVE:", wallet_name + " " + wallet_address + " " + wallet_balance + " ");
        return wallet_string;
    }

    //add new wallet
    //it uses wallet_name and wallet_address arguments, adds to current wallet list from sharepreferences
    public void add_to_wallets(String wallet_name, String wallet_address) throws JSONException{

        jsonObj = new JSONObject();
        try {
            jsonObj.put("title", wallet_name);
            jsonObj.put("notice", "View wallet to check balance.");
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
    //remove wallet
    public void remove_wallet(int wallet_id) throws JSONException{
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
