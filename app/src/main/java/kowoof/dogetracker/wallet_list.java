package kowoof.dogetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;


/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

/**
 * In wallet list we load current saved wallets into listView, calculating total balance
 * We also can change balance from doges to $ by klicking R.id.dollars
 * If we click R.id.refresh, we get new fresh balances using dogechain.info API
 * with request made with wallet_balance object. We'll save balance with wallet_memory object.
 */

public class wallet_list extends DrawerActivity {
    //We create here stuff for listView
    ArrayList<String> title_array = new ArrayList<>();
    ArrayList<String> notice_array = new ArrayList<>();
    ListView list;
    wallet_list_create adapter;

    //TODO better organize this variables
    wallet_memory wallet_memory_handler; //object for saving and reading wallets fro memory
    wallet_balance local_wallet_balance_handler = new wallet_balance(); //object for getting wallet balances
    String wallet_name, wallet_address, wallet_balance;
    int count = 0, wallets_amount = 0, doges_dollars = 1;
    float total_doges = 0;
    private FloatingActionMenu float_menu;
    SwipeRefreshLayout mSwipeRefreshView;

    int finished_update_flag = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        finished_update_flag = 1;
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

        //Floating wallet add menu
        float_menu = findViewById(R.id.floatingMenu);
        float_menu.setClosedOnTouchOutside(true);
        //go to add real wallet
        FloatingActionButton fab = findViewById(R.id.add_real_wallet);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                startActivity(i);
            }
        });
        //go to add virtual wallet
        FloatingActionButton fab2 = findViewById(R.id.add_virtual_wallet);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Coming soon.", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        });

        //Give wallet memory 'handler' current context
        wallet_memory_handler = new wallet_memory(getApplicationContext(), null);

        //Find listView and populate it
        list = findViewById(R.id.wallets);
        populate_list();

        //Go to specific wallet view when you click on stuff on list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(finished_update_flag == 1) { //we would love to wait until all wallets will get updated
                    Intent i = new Intent(getApplicationContext(), wallet_view.class);
                    read_wallet(position);
                    i.putExtra("wallet_id", position);
                    i.putExtra("wallet_name", wallet_name); //show wallet_view what wallet I wanna see
                    i.putExtra("wallet_address", wallet_address); //show wallet_view what wallet I wanna see
                    startActivity(i);
                    finish();
                } else { //Inform user that he should wait to finish updating
                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Please wait for finishing wallets update.", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });
        mSwipeRefreshView = findViewById(R.id.swiperefresh);
        mSwipeRefreshView.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                            get_balances();
                    }
                }
        );
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
            mSwipeRefreshView.setRefreshing(true);
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
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean test = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!test) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
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
        //we change here current fiat currency symbol
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String fiat_name = sp.getString("fiat_list","USD");
        Locale.setDefault(new Locale("lv","LV"));
        Currency c  = Currency.getInstance(fiat_name);

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
            toolbar.setSubtitle("Total: " + total_dollar_doge_s + " " + c.getSymbol());
            doges_dollars = 2;
        }
    }

    //We get here new all wallet balances, checking and updating in view them one by one,
    void get_balances(){
        finished_update_flag = 0;
        //We create handler to wait for get exchange rates
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                try {
                    wallet_balance = local_wallet_balance_handler.balance; //get single wallet balance when you get it from json query
                    wallet_memory_handler.save_to_wallet(wallet_name, wallet_address , wallet_balance, count ); //save it to json
                    update_single_row(count, wallet_name, wallet_balance + " Đ"); //we update signle row in listview
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                count++;
                if(count < wallets_amount){
                    get_balances(); //if there are still wallets to read, get another
                } else {
                    count = 0; //if no, just fill listview
                    mSwipeRefreshView.setRefreshing(false);
                    finished_update_flag = 1;
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
                    update_single_row(count, wallet_name, "Loading balance...");
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

    //Single listview row update
    public void update_single_row(int position, String title, String balance){
        View v = list.getChildAt(position - list.getFirstVisiblePosition());
        if(v == null)
            return;
        TextView title2 = v.findViewById(R.id.wallet_name); // title
        title2.setText(title);
        TextView title22 = v.findViewById(R.id.wallet_doges); // notice
        title22.setText(balance);
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

}
