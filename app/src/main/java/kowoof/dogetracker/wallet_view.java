package kowoof.dogetracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import org.json.JSONException;

import java.util.Currency;
import java.util.Locale;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */


public class wallet_view extends DrawerActivity {

    private static final String qrReadingURL = "https://dogechain.info/api/v1/address/qrcode/";

    private String viewedWalletName, viewedWalletAddress;
    private int viewedWalletId;
    private wallet_balance walletBalanceObject = new wallet_balance();
    private wallet_memory walletMemoryObject;
    private Handler getBalanceHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWalletInfo();
        //Prepare view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Wallet view");
        toolbar.setSubtitle("");

        TextView walletNameTextView = findViewById(R.id.wallet_name);
        TextView walletAddressTextView = findViewById(R.id.wallet_address);
        walletNameTextView.setText(viewedWalletName);
        if(viewedWalletName.equals(viewedWalletAddress)) walletNameTextView.setText("");
        walletAddressTextView.setText(viewedWalletAddress);
        walletMemoryObject = new wallet_memory(getApplicationContext());
        removeWalletButtonHandler();
        //We create handler to wait for get exchange rates
        getBalanceHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                showBalance();
            }
        };

        //Get single wallet balance
        getBalance();

        //QR code download&set section
        ImageView current_wallet_qrcode = findViewById(R.id.imageView2);
        Picasso.with(this).load(qrReadingURL + viewedWalletAddress).into(current_wallet_qrcode);
    }

    public void getWalletInfo(){
        Bundle wallet_list_feedback = getIntent().getExtras();
        if (wallet_list_feedback != null)
        {
            viewedWalletName = wallet_list_feedback.getString("wallet_name");
            viewedWalletAddress = wallet_list_feedback.getString("wallet_address");
            viewedWalletId = wallet_list_feedback.getInt("wallet_id");
        }
    }
    public void removeWalletButtonHandler(){
        //Set button for deleting wallet (just from viewer, not really lol)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(wallet_view.this);
                builder.setCancelable(true);
                builder.setTitle("Are you sure?");
                builder.setMessage("Remove wallet: " + viewedWalletName);
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    walletMemoryObject.removeWallet(viewedWalletId);
                                    Intent i = new Intent(getApplicationContext(), wallet_list.class);
                                    startActivity(i);
                                    finish();
                                } catch (JSONException e) {

                                }
                            }
                        });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    // Letting come back home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean use_background_logo_setting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!use_background_logo_setting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // handle Android back click here
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    public void getBalance(){
        walletBalanceObject.getWalletBalance(this, getBalanceHandler, viewedWalletAddress);
    }
    public void showBalance(){
        TextView wallet_balance_textView = findViewById(R.id.balance);
        wallet_balance_textView.setText(walletBalanceObject.balance + " Đ");
        balanceInFiat();
    }
    public void balanceInFiat(){
        doge_rates get_doge_dollar_rate = new doge_rates(getApplicationContext());
        get_doge_dollar_rate.readRatesFromOffline();
        float dolar_doge_f = Float.parseFloat(get_doge_dollar_rate.dogeFiatRate);
        float balance_f = Float.parseFloat(walletBalanceObject.balance);
        float total_dollar_balance_f = dolar_doge_f * balance_f;
        String dollar_doge_s = Float.toString(total_dollar_balance_f);
        TextView walletFiatBalanceTextView = findViewById(R.id.doge_in_dollars);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String fiat_name = sp.getString("fiat_list","USD");
        Locale.setDefault(new Locale("lv","LV"));
        Currency used_fiat_currency  = Currency.getInstance(fiat_name);
        walletFiatBalanceTextView.setText(dollar_doge_s + " " + used_fiat_currency.getSymbol());
    }
    //Copy wallet address by clicking qr code
    public void copyWalletAddress(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", viewedWalletAddress);
        clipboard.setPrimaryClip(clip);

        ConstraintLayout layout = findViewById(R.id.snackbar_layout_view);
        Snackbar snackbar = Snackbar
                .make(layout, "Address copied to clipboard.", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}

