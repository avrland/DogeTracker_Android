package kowoof.dogetracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;

import java.lang.ref.WeakReference;

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
    private ProgressDialog addWalletProgressDialog;

    private static String devDogecoinAddress = "DTvQCPbJ1UwVxdV3Gei9u4FWHpdcGKsWYS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add);
        setToolbar();

        EditText walletNameEditText = findViewById(R.id.editText10);
        walletNameEditText.requestFocus();

        walletMemoryObject = new wallet_memory(getApplicationContext());
        handler = new WalletMemoryHandler(this);
        addWalletProgressDialog = new ProgressDialog(wallet_add.this);
        addWalletProgressDialog.setCancelable(false);

        addWalletFabHandler();
        checkIfQrReceived();
        pasteDevAddressListener();
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        checkLogoSetting();
    }
    public void onPause(){
        super.onPause();
        addWalletProgressDialog.dismiss();
    }
    private void checkLogoSetting(){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", true);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }

    private static class WalletMemoryHandler extends Handler {
        private final WeakReference<wallet_add> mActivity;

        private WalletMemoryHandler(wallet_add activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            wallet_add activity = mActivity.get();
            if (activity != null) {
                try {
                    activity.walletMemoryObject.addToWalletsWithBalance(activity.addedWalletName, activity.addedWalletAddress, activity.currentWalletBalance.balance);
                    Intent i = new Intent(activity.getApplicationContext(), wallet_list.class);
                    i.putExtra("added_wallet", 1);
                    activity.startActivity(i);
                    activity.finish();
                } catch (JSONException e) {
                    activity.makeSnackbar("Connection error.");
                }
            }
        }
    }

    public void addWalletFabHandler(){
        FloatingActionButton addWalletFab = findViewById(R.id.fab);
        //We get here wallet address and name, and save it to SharedPref
        addWalletFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addWalletProgressDialog = ProgressDialog.show(wallet_add.this, getString(R.string.pleaseWaitText), getString(R.string.validatingText));
                EditText walletNameEditText = findViewById(R.id.editText10);
                addedWalletName = walletNameEditText.getText().toString();
                EditText walletAddressEditText = findViewById(R.id.editText);
                addedWalletAddress = walletAddressEditText.getText().toString();
                ifNameEmptyAddAddressAsName();
                verifyWalletAddress();
            }
        });
    }
    public void ifNameEmptyAddAddressAsName(){
        if(addedWalletName.trim().length() == 0) addedWalletName = addedWalletAddress;
    }
    public void verifyWalletAddress(){
        if(wallet_verify.validateDogecoinAddress(addedWalletAddress)){
            currentWalletBalance.getWalletBalance(this, handler, addedWalletAddress);
        } else {
            addWalletProgressDialog.dismiss();
            makeSnackbar(getString(R.string.invalidAddressText));
        }
    }
    public void checkIfQrReceived(){
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
    }
    public void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.addRealWalletText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
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
        makeSnackbar(getString(R.string.addressPastedMessege));
    }
    private void pasteDevAddressListener(){
        final EditText editText = findViewById(R.id.editText);
        editText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editText.setText(devDogecoinAddress);
                makeSnackbar(getString(R.string.devAddressPastedMessege));
                return true;
            }
        });
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
                    makeSnackbar(getString(R.string.grantPermissionText));
                }
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
