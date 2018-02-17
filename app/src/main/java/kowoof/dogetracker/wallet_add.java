package kowoof.dogetracker;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.icu.util.Currency;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */


public class wallet_add extends AppCompatActivity {

    //We create object to save stuff to memory
    private wallet_memory walletMemoryObject;
    //We create strings to store address and name before adding
    private String addedWalletName, addedWalletAddress;
    //We create handler to react after getting first time balance
    private Handler handler = new Handler();
    //We create object to get first time balance (next time we'll do it in wallet_list and wallet_view)
    private wallet_balance currentWalletBalance = new wallet_balance();
    //We create object from class to verify if Dogecoin address is valid
    private wallet_verify walletAddressVerify = new wallet_verify();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add);
        setToolbar("Add real wallet", null);
        EditText walletNameEditText = findViewById(R.id.editText2);
        walletNameEditText.requestFocus();
        walletMemoryObject = new wallet_memory(getApplicationContext(), null);
        FloatingActionButton addWalletFab = findViewById(R.id.fab);
        //We get here wallet address and name, and save it to SharedPref
        addWalletFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText walletNameEditText = findViewById(R.id.editText2);
                addedWalletName = walletNameEditText.getText().toString();
                EditText walletAddressEditText = findViewById(R.id.editText);
                addedWalletAddress = walletAddressEditText.getText().toString();
                if(addedWalletName.trim().length() == 0) addedWalletName = addedWalletAddress;
                if(walletAddressVerify.validateDogecoinAddress(addedWalletAddress)==true){
                    currentWalletBalance.get_wallet_balance(wallet_add.this, handler, addedWalletAddress);
                } else {
                    makeSnackbar("Invalid address.");
                }
            }
        });

        //If we use qr code reader, we insert here scanned address
        Bundle qrReaderMessage = getIntent().getExtras();
        if(qrReaderMessage!=null) {
            int checkIfReadedQr = qrReaderMessage.getInt("readed_qr_code");
            if (checkIfReadedQr==1) {
                addedWalletAddress = qrReaderMessage.getString("wallet_address");
                EditText editText = findViewById(R.id.editText);
                editText.setText(addedWalletAddress);
            }
        }


        //We create handler to wait for get exchange rates
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                try {
                    walletMemoryObject.addToWalletsWithBalance(addedWalletName, addedWalletAddress, currentWalletBalance.balance);
                    Intent i = new Intent(getApplicationContext(), wallet_list.class);
                    i.putExtra("added_wallet", 1);
                    startActivity(i);
                    finish();
                } catch (JSONException e) {
                    makeSnackbar("Connection error.");
                }
            }

        };
    }
    public void setToolbar(String title, String subtitle){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        if(subtitle != null) getSupportActionBar().setSubtitle(subtitle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Letting come back home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    //if we have address copied to clipboard, we can paste it here
    public void pasteWalletAddress(View view) {
        ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData abc = clipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String pastedWalletAddress = item.getText().toString();
        EditText editText = findViewById(R.id.editText);
        editText.setText(pastedWalletAddress);
        makeSnackbar("Address pasted.");
    }

    //if we want to scan, we go to wallet_qr_read
    public void scanQrCode(View view) {
        ActivityCompat.requestPermissions(wallet_add.this,
                new String[]{Manifest.permission.CAMERA},
                1);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent i = new Intent(getApplicationContext(), wallet_qr_read.class);
                    startActivity(i);
                } else {
                    makeSnackbar("Grant permission to camera to read qr code");
                }
                return;
            }
        }
    }
    //toast function to get it a little bit shorter
    public void makeSnackbar(String snackbar_message){
        Snackbar snackbar = Snackbar
                .make(getWindow().getDecorView(), snackbar_message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
