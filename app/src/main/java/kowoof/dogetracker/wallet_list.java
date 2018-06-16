package kowoof.dogetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Locale;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 *
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

    private wallet_memory walletMemoryObject; //object for saving and reading wallets fro memory
    private String walletName, walletAddress, walletBalance;
    private int dogesFiat = 1;
    private SwipeRefreshLayout mSwipeRefreshView;
    private Toolbar toolbar;
    int finishedUpdateFlag = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_list);
        finishedUpdateFlag = 1;
        setToolbar();
        setDrawer();

        fabButtonsHandler();
        goToWalletViewHandler();
        useSwipeRefreshHandler();

        //Give wallet memory 'handler' current context
        Handler balanceHandler = new WalletMemoryHandler(this);
        walletMemoryObject = new wallet_memory(getApplicationContext(), balanceHandler);

        //Find listView and populate it
        populateList();
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean use_background_logo_setting = spref.getBoolean("dt_logo", true);
        ImageView logo = findViewById(R.id.imageView);
        if(!use_background_logo_setting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
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
            mSwipeRefreshView.setRefreshing(true);
            refreshAllWalletsBalances();
        }
        if (id == R.id.dollars) {
            if(dogesFiat == 1) {
                showTotalBalanceInFiatOnToolbar(walletMemoryObject.calculateAllWalletsBalance());
            } else showTotalBalanceInDogesOnToolbar(walletMemoryObject.calculateAllWalletsBalance());
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
    public void fabButtonsHandler(){
        FloatingActionMenu floatMenu;
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
                finish();
                Intent i = new Intent(getApplicationContext(), wallet_add_virtual.class);
                startActivity(i);
            }
        });
    }
    public void useSwipeRefreshHandler(){
        mSwipeRefreshView = findViewById(R.id.swiperefresh);
        mSwipeRefreshView.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshAllWalletsBalances();
                    }
                }
        );
    }

    private void goToWalletViewHandler(){
        //Go to specific wallet view when you click on stuff on list
        list = findViewById(R.id.wallets);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(finishedUpdateFlag == 1) {
                    Intent passWalletInfo = new Intent(getApplicationContext(), wallet_view.class);
                    getWalletNameAddress(position);
                    passWalletInfo.putExtra("wallet_id", position);
                    passWalletInfo.putExtra("wallet_name", walletName);
                    passWalletInfo.putExtra("wallet_address", walletAddress);
                    passWalletInfo.putExtra("wallet_balance", walletBalance);
                    startActivity(passWalletInfo);
                    finish();
                } else {
                    makeSnackbar(getString(R.string.waitToFinishUpdateText));
                }
            }
        });
    }
    private void getWalletNameAddress(int number){
        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.readAllWallets());
            JSONObject jsonObject = new_array.getJSONObject(number);
            walletName = jsonObject.getString("title");
            walletAddress = jsonObject.getString("address");
            walletBalance = jsonObject.getString("notice");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //we show here total balance on to toolbar
    private void setToolbar(){
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.myWalletsMenuText));
        toolbar.setSubtitle(getString(R.string.totalBalanceText, " ","Đ"));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void showTotalBalanceInFiatOnToolbar(float total){
        //we change here current fiat currency symbol
        doge_rates local_doge = new doge_rates(wallet_list.this);

        float totalFiatBalance = local_doge.getDogeFiatRate() * total;
        String totalFiatDogeString = Float.toString(totalFiatBalance);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(getString(R.string.totalBalanceText, totalFiatDogeString,local_doge.getFiatSymbol()));
        dogesFiat = 2;
    }
    private void showTotalBalanceInDogesOnToolbar(float total){
        //we change here current fiat currency symbol
        String totalDogeString = Float.toString(total);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setSubtitle(getString(R.string.totalBalanceText, totalDogeString,"Đ"));
        dogesFiat = 1;
    }
    private void setDrawer(){
        //we add it the same stuff as in DrawerActivity because it's getting overwritten and hamburger button doesn't works
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }


    //Stuff needed to fetch new data
    private void refreshAllWalletsBalances(){
        walletMemoryObject.getBalances();
    }
    private static class WalletMemoryHandler extends Handler {
        private final WeakReference<wallet_list> mActivity;

        private WalletMemoryHandler(wallet_list activity) {
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            wallet_list activity = mActivity.get();
            if (activity != null) {
                int response = msg.arg1;
                if(response==1){
                    activity.updateSingleRow(activity.walletMemoryObject.COUNT, activity.walletMemoryObject.WALLET_NAME, activity.getString(R.string.loadingBalanceText));
                } else if (response==2){
                    Log.d("Wallet name: ", activity.walletMemoryObject.WALLET_NAME);
                    Log.d("Wallet address: ", activity.walletMemoryObject.WALLET_ADDRESS);
                    Log.d("Wallet balance: ", activity.walletMemoryObject.WALLET_BALANCE);
                    Log.d("Count: : ", Integer.toString(activity.walletMemoryObject.COUNT));

                    String addedListViewBalance = activity.walletMemoryObject.WALLET_BALANCE + " Đ";
                    if(!activity.walletMemoryObject.WALLET_ADDRESS.equals("Virtual")) {
                        activity.updateSingleRow(activity.walletMemoryObject.COUNT, activity.walletMemoryObject.WALLET_NAME, activity.walletMemoryObject.WALLET_BALANCE + " Đ");
                    } else activity.updateSingleRow(activity.walletMemoryObject.COUNT, activity.walletMemoryObject.WALLET_NAME, activity.walletMemoryObject.WALLET_BALANCE + " Đ (Virtual)");
                    activity.walletMemoryObject.COUNT++;
                    if (activity.walletMemoryObject.COUNT < activity.walletMemoryObject.wallets_amount) {
                        //We still have wallets to update, so we order another update
                        activity.walletMemoryObject.getBalances(); //if there are still wallets to read, get another
                    } else {
                        //Finished getting all balances, so we send info we're ready
                        activity.mSwipeRefreshView.setRefreshing(false);
                        activity.walletMemoryObject.COUNT = 0;
                    }
                } else if (response==0){
                    activity.mSwipeRefreshView.setRefreshing(false);
                }
            }
        }
    }

    //ListView managment section
    private void populateList(){
        clearWalletsList();
        //prepare all balances float handler
        float total_balance_f = 0, current_wallet_f = 0;
        try {
            JSONArray new_array = new JSONArray(walletMemoryObject.readAllWallets());
            for (int i = 0, count = new_array.length(); i < count; i++) {
                try {
                    JSONObject jsonObject = new_array.getJSONObject(i);
                    String currentWalletAddress = jsonObject.getString("address");
                    walletNameArray.add(jsonObject.getString("title"));
                    try {
                        current_wallet_f = Float.parseFloat(jsonObject.getString("notice"));
                        if(!currentWalletAddress.equals("Virtual")) {
                            balanceArray.add(jsonObject.getString("notice") + " Đ");
                        } else balanceArray.add(jsonObject.getString("notice") + " Đ (Virtual)");
                    } catch (NumberFormatException e) {
                        balanceArray.add("Error");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                total_balance_f += current_wallet_f;
            }
            adapter = new wallet_list_create(wallet_list.this, walletNameArray, balanceArray);
            list.setAdapter(adapter);
            showTotalBalanceInDogesOnToolbar(total_balance_f);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    private void clearWalletsList(){
        adapter = new wallet_list_create(wallet_list.this, walletNameArray, balanceArray);
        walletNameArray.clear();
        balanceArray.clear();
        list.setAdapter(adapter);
    }
    private void updateSingleRow(int position, String title, String balance){
        View v = list.getChildAt(position - list.getFirstVisiblePosition());
        if(v == null) return;
        TextView title2 = v.findViewById(R.id.wallet_name); // title
        title2.setText(title);
        TextView title22 = v.findViewById(R.id.wallet_doges); // notice
        title22.setText(balance);
    }

    //toast function to get it a little bit shorter
    public void makeSnackbar(String snackbar_message){
        Snackbar snackbar = Snackbar
                .make(getWindow().getDecorView(), snackbar_message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}
