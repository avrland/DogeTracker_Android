package kowoof.dogetracker;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;


/**
 * In wallet list we load current saved wallets into listView, calculating total balance
 * We also can change balance from doges to $ by klicking R.id.dollars
 * If we click R.id.refresh, we get new fresh balances using dogechain.info API
 * with request made with wallet_balance object. We'll save balance with wallet_memory object.
 */

public class wallet_list extends DrawerActivity {
    //We create here stuff for listView
    ArrayList<String> title_array = new ArrayList<String>();
    ArrayList<String> notice_array = new ArrayList<String>();
    ListView list;
    wallet_list_create adapter;

    //TODO better organize this variables
    wallet_memory wallet_memory_handler; //object for saving and reading wallets fro memory
    wallet_balance local_wallet_balance_handler = new wallet_balance(); //object for getting wallet balances
    String wallet_name, wallet_address, wallet_balance;
    int count = 0, wallets_amount = 0, doges_dollars = 1;
    float total_doges = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);

        // Make toolbar wow again, I wanted to add here total amount of doges
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My wallets");
        toolbar.setSubtitle("Total:  Đ");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //we add it the same stuff as in DrawerActivity because it's getting overwritten and hamburger button doesn't works
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // Add wallet button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                startActivity(i);
            }
        });
        //Give wallet memory 'handler' current context
        wallet_memory_handler = new wallet_memory(getApplicationContext());

        //Find listView and populate it
        list = findViewById(R.id.wallets);
        populate_list();

        //Go to specific wallet view when you click on stuff on list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(getApplicationContext(), wallet_view.class);
                read_wallet(position);
                i.putExtra("wallet_id", position);
                i.putExtra("wallet_name", wallet_name); //show wallet_view what wallet I wanna see
                i.putExtra("wallet_address", wallet_address); //show wallet_view what wallet I wanna see
                startActivity(i);
                finish();
            }
        });
    }

    //Opening drawer here
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Letting come back home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        }
        if (id == R.id.refresh) {
            //Refresh all wallets data
            total_doges = 0;
            get_balances();
        }
        if (id == R.id.dollars) {
            if(doges_dollars == 1) {
                //show total balance in dollars
                total_balance(total_doges, 2);
                doges_dollars = 2;
            } else {
                //show total balance in doges
                total_balance(total_doges, 1);
                doges_dollars = 1;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onResume() {
        super.onResume();
    }

    //populate list with items saved into json
    void populate_list(){
        //clear listview every time you want to fill it
        adapter = new wallet_list_create(wallet_list.this, title_array, notice_array);
        title_array.clear();
        notice_array.clear();
        list.setAdapter(adapter);
        //prepare all balances float handler
        float total_balance_f = 0;
        float current_wallet_f = 0;

        try {
            JSONArray new_array = new JSONArray(wallet_memory_handler.read_all_wallets());

            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    title_array.add(jsonObject.getString("title"));
                    try {
                        current_wallet_f = Float.parseFloat(jsonObject.getString("notice"));
                        notice_array.add(jsonObject.getString("notice") + " Đ");
                    } catch (NumberFormatException e) {
                        notice_array.add(jsonObject.getString("notice"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                total_balance_f += current_wallet_f;
            }
            adapter = new wallet_list_create(wallet_list.this, title_array, notice_array);
            list.setAdapter(adapter);
            total_balance(total_balance_f, 1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        total_doges = total_balance_f;
    }

    //we show here total balance on to toolbar
    public void total_balance(float total, int doges_or_dollars){
        doge_rates get_doge_dollar_rate = new doge_rates(getApplicationContext());
        get_doge_dollar_rate.read_rates_from_offline();
        float dolar_doge_f = Float.parseFloat(get_doge_dollar_rate.doge_rate);
        float total_dollar_balance = dolar_doge_f * total;
        String total_dollar_doge_s = Float.toString(total_dollar_balance);
        String total_doges_s = Float.toString(total);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(doges_or_dollars == 1){
            toolbar.setSubtitle("Total: " + total_doges_s + " Đ");
            doges_dollars = 1;
        }
        if(doges_or_dollars == 2){
            toolbar.setSubtitle("Total: " + total_dollar_doge_s + " $");
            doges_dollars = 2;
        }
    }

    //We get here new all wallet balances, checking them one by one
    void get_balances(){
        //We create handler to wait for get exchange rates
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                try {
                    wallet_balance = local_wallet_balance_handler.balance; //get single wallet balance when you get it from json query
                    wallet_memory_handler.save_to_wallet(wallet_name, wallet_address , wallet_balance, count ); //save it to json
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                count++;
                if(count < wallets_amount){
                    get_balances(); //if there are still wallets to read, get another
                } else {
                    count = 0; //if no, just fill listview
                    populate_list();
                }
            }

        };
        try {
            JSONArray new_array = new JSONArray(wallet_memory_handler.read_all_wallets());
            wallets_amount = new_array.length();
                try {
                    JSONObject jsonObject = new_array.getJSONObject(count);
                    wallet_name = jsonObject.getString("title");
                    wallet_address = jsonObject.getString("address");
                    //send get balance query with current address, wait in handler for response
                    local_wallet_balance_handler.get_wallet_balance(this, handler, wallet_address);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //read wallet name and address by position
    void read_wallet(int number){
        try {
            JSONArray new_array = new JSONArray(wallet_memory_handler.read_all_wallets());
            JSONObject jsonObject = new_array.getJSONObject(number);
            wallet_name = jsonObject.getString("title");
            wallet_address = jsonObject.getString("address");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // Last but not least, useful stuff to make app working
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, messege_toast, duration);
        toast.show();
    }
}
