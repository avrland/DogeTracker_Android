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
    private ArrayList<String> walletNameArray = new ArrayList<>();
    private ArrayList<String> balanceArray = new ArrayList<>();
    private ListView list;
    private wallet_list_create adapter;

    //TODO better organize this variables
    private wallet_memory walletMemoryObject; //object for saving and reading wallets fro memory
    private wallet_balance walletBalanceHandler = new wallet_balance(); //object for getting wallet balances
    private String walletName, walletAddress, walletBalance;
    private int count = 0, walletsAmount = 0, dogesFiat = 1;
    private float totalDoges = 0;
    private FloatingActionMenu floatMenu;
    private SwipeRefreshLayout mSwipeRefreshView;

    int finishedUpdateFlag = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        finishedUpdateFlag = 1;
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

        floatMenu = findViewById(R.id.floatingMenu);
        floatMenu.setClosedOnTouchOutside(true);
        FloatingActionButton realWalletFab = findViewById(R.id.add_real_wallet);
        realWalletFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                startActivity(i);
            }
        });
        FloatingActionButton virtualWalletFab = findViewById(R.id.add_virtual_wallet);
        virtualWalletFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Coming soon.", Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        });

//        Handler balance_handler = new Handler(){
//            @Override
//            public void handleMessage(Message msg) {
//                super.handleMessage(msg);
//                if(msg.arg1== 1){
//                    update_single_row(wallet_memory_object.COUNT, wallet_memory_object.WALLET_NAME, "Loading....");
//                }
//                if(msg.arg1== 2){ //
//                    update_single_row(wallet_memory_object.COUNT, wallet_memory_object.WALLET_NAME, wallet_memory_object.WALLET_BALANCE);
//                }
//                if(msg.arg1==3){
//                    finished_update_flag = 1;
//                    wallet_memory_object.COUNT = 0;
//                    mSwipeRefreshView.setRefreshing(false);
//                }
//            }
//
//        };


        //Give wallet memory 'handler' current context
        walletMemoryObject = new wallet_memory(getApplicationContext(), null);

        //Find listView and populate it
        list = findViewById(R.id.wallets);
        populateList();

        //Go to specific wallet view when you click on stuff on list
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(finishedUpdateFlag == 1) {
                    Intent i = new Intent(getApplicationContext(), wallet_view.class);
                    getWalletNameAddress(position);
                    i.putExtra("wallet_id", position);
                    i.putExtra("wallet_name", walletName);
                    i.putExtra("wallet_address", walletAddress);
                    startActivity(i);
                    finish();
                } else {
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
                            getBalances();
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
            totalDoges = 0;
            getBalances();
            mSwipeRefreshView.setRefreshing(true);
        }
        if (id == R.id.dollars) {
            //1 = doges, 2 = fiat
            if(dogesFiat == 1) {
                showTotalBalanceOnToolbar(totalDoges, 2);
                dogesFiat = 2;
            } else {
                showTotalBalanceOnToolbar(totalDoges, 1);
                dogesFiat = 1;
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
        boolean use_background_logo_setting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!use_background_logo_setting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }

    //populate list with items saved into json
    void populateList(){
        //clear listview every time you want to fill it
        adapter = new wallet_list_create(wallet_list.this, walletNameArray, balanceArray);
        walletNameArray.clear();
        balanceArray.clear();
        list.setAdapter(adapter);

        //prepare all balances float handler
        float total_balance_f = 0;
        float current_wallet_f = 0;

        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.read_all_wallets());

            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    walletNameArray.add(jsonObject.getString("title"));
                    try {
                        current_wallet_f = Float.parseFloat(jsonObject.getString("notice"));
                        balanceArray.add(jsonObject.getString("notice") + " Đ");
                    } catch (NumberFormatException e) {
                        balanceArray.add(jsonObject.getString("notice"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                total_balance_f += current_wallet_f;
            }
            adapter = new wallet_list_create(wallet_list.this, walletNameArray, balanceArray);
            list.setAdapter(adapter);
            showTotalBalanceOnToolbar(total_balance_f, 1);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        totalDoges = total_balance_f;
    }

    //we show here total balance on to toolbar
    public void showTotalBalanceOnToolbar(float total, int doges_or_dollars){
        //we change here current fiat currency symbol
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String fiatCode = sp.getString("fiat_list","USD");
        Locale.setDefault(new Locale("lv","LV"));
        Currency usedFiatCurrency  = Currency.getInstance(fiatCode);

        doge_rates getDogeFiatRate = new doge_rates(getApplicationContext());
        getDogeFiatRate.read_rates_from_offline();
        float fiatDogeFloat = Float.parseFloat(getDogeFiatRate.doge_rate);
        float totalFiatBalance = fiatDogeFloat * total;
        String totalFiatDogeString = Float.toString(totalFiatBalance);
        String totalDogeString = Float.toString(total);
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(doges_or_dollars == 1){
            toolbar.setSubtitle("Total: " + totalDogeString + " Đ");
            dogesFiat = 1;
        }
        if(doges_or_dollars == 2){
            toolbar.setSubtitle("Total: " + totalFiatDogeString + " " + usedFiatCurrency.getSymbol());
            dogesFiat = 2;
        }
    }

    //We get here new all wallet balances, checking and updating in view them one by one,
    void getBalances(){
        finishedUpdateFlag = 0;
//        wallet_memory_object.get_balances();
        //We create handler to wait for get exchange rates
        Handler handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                try {
                    walletBalance = walletBalanceHandler.balance; //get single wallet balance when you get it from json query
                    walletMemoryObject.save_to_wallet(walletName, walletAddress , walletBalance, count ); //save it to json
                    updateSingleRow(count, walletName, walletBalance + " Đ"); //we update signle row in listview
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                count++;
                if(count < walletsAmount){
                    getBalances(); //if there are still wallets to read, get another
                } else {
                    count = 0; //if no, just fill listview
                    mSwipeRefreshView.setRefreshing(false);
                    finishedUpdateFlag = 1;
                }
            }

        };
        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.read_all_wallets());
            walletsAmount = new_array.length();
                try {
                    JSONObject jsonObject = new_array.getJSONObject(count);
                    walletName = jsonObject.getString("title");
                    walletAddress = jsonObject.getString("address");
                    updateSingleRow(count, walletName, "Loading balance...");
                    //send get balance query with current address, wait in handler for response
                    walletBalanceHandler.get_wallet_balance(this, handler, walletAddress);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Single listview row update
    public void updateSingleRow(int position, String title, String balance){
        View v = list.getChildAt(position - list.getFirstVisiblePosition());
        if(v == null)
            return;
        TextView title2 = v.findViewById(R.id.wallet_name); // title
        title2.setText(title);
        TextView title22 = v.findViewById(R.id.wallet_doges); // notice
        title22.setText(balance);
    }

    //read wallet name and address by position
    void getWalletNameAddress(int number){
        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.read_all_wallets());
            JSONObject jsonObject = new_array.getJSONObject(number);
            walletName = jsonObject.getString("title");
            walletAddress = jsonObject.getString("address");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
