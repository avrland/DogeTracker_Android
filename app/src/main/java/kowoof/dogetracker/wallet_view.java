package kowoof.dogetracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.net.Uri;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.squareup.picasso.Picasso;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Currency;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */


public class wallet_view extends DrawerActivity {

    private String viewedWalletName, viewedWalletAddress, viewedWalletBalance;
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
        setToolbar();
        insertWalletInfoIntoView();
        removeWalletButtonHandler();

        walletMemoryObject = new wallet_memory(getApplicationContext());
        getBalanceHandler = new WalletViewHandler(this);
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
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        checkLogoSetting();
        if(viewedWalletAddress.equals("Virtual")){
            handleVirtualWallet();
        } else handleRealWallet();
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

    private void getWalletInfo(){
        Bundle walleListFeedback = getIntent().getExtras();
        if (walleListFeedback != null)
        {
            viewedWalletName = walleListFeedback.getString("wallet_name");
            viewedWalletAddress = walleListFeedback.getString("wallet_address");
            viewedWalletId = walleListFeedback.getInt("wallet_id");
            viewedWalletBalance = walleListFeedback.getString("wallet_balance");
        }
    }
    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_wallet_view));
        toolbar.setSubtitle("");
    }
    private void checkLogoSetting(){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }
    private boolean checkMergedQRCodeSetting(){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useMergedQRCodeSetting = spref.getBoolean("mergedQRCode", true);
        return useMergedQRCodeSetting;
    }
    private void removeWalletButtonHandler(){
        //Set button for deleting wallet (just from viewer, not really lol)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(wallet_view.this);
                builder.setCancelable(true);
                builder.setTitle(getString(R.string.areYouSureText));
                builder.setMessage(getString(R.string.removeWalletText) + viewedWalletName);
                builder.setPositiveButton(getString(R.string.confirmText),
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
                builder.setNegativeButton(getString(R.string.cancelText), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    private void insertWalletInfoIntoView(){
        TextView walletNameTextView = findViewById(R.id.wallet_name);
        TextView walletAddressTextView = findViewById(R.id.wallet_address);
        walletNameTextView.setText(viewedWalletName);
        if(viewedWalletName.equals(viewedWalletAddress)) walletNameTextView.setText("");
        walletAddressTextView.setText(viewedWalletAddress);
    }

    public void viewOnDogechain(View view) {
        Uri uri = Uri.parse("https://dogechain.info/address/" + viewedWalletAddress);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private static class WalletViewHandler extends Handler {
        private final WeakReference<wallet_view> mActivity;

        private WalletViewHandler(wallet_view activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            wallet_view activity = mActivity.get();
            if (activity != null) {
                activity.showBalance();
            }
        }
    }
    private void handleRealWallet(){
        //Get single wallet balance
        getBalance();
        //QR code download&set section
        ImageView current_wallet_qrcode = findViewById(R.id.imageView2);
        if(checkMergedQRCodeSetting()) {
            Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.doge_meme);
            Bitmap logo_wyjsciowe = CuteR.Product(viewedWalletAddress, logo, false, 0xFF000000);
            current_wallet_qrcode.setImageBitmap(logo_wyjsciowe);
        } else {
            Bitmap logo_wyjsciowe = CuteR.ProductNormal(viewedWalletAddress, false, 0xFF000000);
            current_wallet_qrcode.setImageBitmap(logo_wyjsciowe);
        }
    }

    private void handleVirtualWallet(){
        walletBalanceObject.balance = viewedWalletBalance;
        showBalance();
        ImageView dogecoinLogo = findViewById(R.id.imageView2);
        dogecoinLogo.setImageResource(R.drawable.dogecoin_logo);

        Button seeOnDogechainButton = findViewById(R.id.button4);
        seeOnDogechainButton.setEnabled(false);
    }
    private void getBalance(){
        walletBalanceObject.getWalletBalance(this, getBalanceHandler, viewedWalletAddress);
    }


    private void showBalance(){
        TextView walletBalanceTextView = findViewById(R.id.balance);
        walletBalanceTextView.setText(walletBalanceObject.balance + " Đ");
        balanceInFiat();
    }
    private void balanceInFiat(){
        doge_rates dogeRateObject = new doge_rates(getApplicationContext());
        float fiatDogeFloat = dogeRateObject.getDogeFiatRate();
        float balanceFloat = Float.parseFloat(walletBalanceObject.balance);
        float totalFiatBalanceFloat = fiatDogeFloat * balanceFloat;
        String fiatDogeStringWithSymbol = Float.toString(totalFiatBalanceFloat) + " " + dogeRateObject.getFiatSymbol();

        TextView walletFiatBalanceTextView = findViewById(R.id.doge_in_dollars);
        walletFiatBalanceTextView.setText(fiatDogeStringWithSymbol);
    }
    //Copy wallet address by clicking qr code
    public void copyWalletAddress(View view) {
        if(!viewedWalletAddress.equals("Virtual")) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", viewedWalletAddress);
            clipboard.setPrimaryClip(clip);

            ConstraintLayout layout = findViewById(R.id.snackbar_layout_view);
            Snackbar snackbar = Snackbar
                    .make(layout, getString(R.string.addressCopiedText), Snackbar.LENGTH_SHORT);
            snackbar.show();
        } else {
            makeSnackbar("Such wow, virtual wallet, no need to copy address.");
        }
    }
}

