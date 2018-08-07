package kowoof.dogetracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class MainActivity extends DrawerActivity {

    private static int defaultTextColor = Color.rgb(0x75, 0x75, 0x75);
    private SharedPreferences spref;

    //We create doge_rates object and handler to make getting exchange rates wow
    private doge_rates dogeRatesObject;
    private Handler getRatesHandler = new Handler();
    private TextView dogeRatesTextView, hourChangeTextView, dailyChangeTextView,
            weeklyChangeTextView, marketCapTextView, volumeTextView,
            totalSupplyTextView, lastUpdateTextView, allWalletsBalanceTextView;
    private wallet_memory walletMemoryObject;
    private ProgressDialog dialog, quickscandialog;
    private boolean startupAutoRefreshDone;
    private String scanned_QRcodeAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        //We create handler with WeakReference to wait for get exchange rates
        Handler balanceHandler = new BalanceHandler(this);
        getRatesHandler = new GetRatesHandler(this);

        walletMemoryObject = new wallet_memory(getApplicationContext(), balanceHandler);
        dogeRatesObject = new doge_rates(this);

        prepareProgressDialog();
        getTextViews();
        rateAppReminder();
        checkFirstRun();
        checkNightModeSetting();
        startup_refresh();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu, this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.quickscan:
                scanQrCode();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        checkLogoSetting();
        checkAllExchangeTrendColors();
        checkFirstRun();
        checkIfQuickScanDone();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( dialog!=null && dialog.isShowing() ){
            dialog.cancel();
        }
    }
    private void prepareProgressDialog(){
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.gettingRatesText));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //TODO add possibility to abort refreshing
//        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "ABORT", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dialog, int id) {
//                walletMemoryObject.COUNT = walletMemoryObject.wallets_amount;
//                readDataFromOffline();
//                dialog.dismiss();
//            }
//        });
    }
    private void launchRefreshBalanceProcess(){
        dialog.show();
        dialog.setProgress(0);
        refreshRates();
        allWalletsBalanceTextView.setText(getString(R.string.refreshingText));
        walletMemoryObject.allWalletsBalance = 0;
        walletMemoryObject.getBalances();
    }

    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling getRates from doge_rates class
    public void refreshButton(View view) {
        launchRefreshBalanceProcess();
    }
    private void refreshRates(){
        //Getting current dogecoin rates from coinmarketcap
        dogeRatesObject.getRates(getRatesHandler, spref.getString("fiat_list","USD"));
    }

    private static class BalanceHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private BalanceHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                int isAnyWalletsAdded = msg.arg1;
                // Handle message code
                if(isAnyWalletsAdded==2){
                    activity.walletMemoryObject.COUNT++;
                    activity.dialog.incrementProgressBy(100/activity.walletMemoryObject.wallets_amount);
                    if (activity.walletMemoryObject.COUNT < activity.walletMemoryObject.wallets_amount) continueRefreshing();
                    else finishRefreshing();
                } else if (isAnyWalletsAdded==0){
                    leaveNoWalletsInformation();
                }  else if (isAnyWalletsAdded==-1){
                    activity.makeSnackbar(activity.getString(R.string.errorText));
                } else if (isAnyWalletsAdded==5){ //we got here single wallet balance (for quickscan)
                    activity.quickscandialog.dismiss();
                    activity.buildQuickScanDoneDialog();
                }
            }
        }
        private void continueRefreshing(){
            MainActivity activity = mActivity.get();
            activity.allWalletsBalanceTextView.setText(activity.walletMemoryObject.allWalletsBalance + " Đ = " + activity.getFiatBalance());
            activity.walletMemoryObject.getBalances();
        }
        private void finishRefreshing(){
            MainActivity activity = mActivity.get();
            activity.allWalletsBalanceTextView.setText(activity.walletMemoryObject.allWalletsBalance + " Đ = " + activity.getFiatBalance());
            activity.dialog.dismiss();
            activity.walletMemoryObject.COUNT = 0;
        }
        private void leaveNoWalletsInformation(){
            MainActivity activity = mActivity.get();
            activity.allWalletsBalanceTextView.setText(activity.getString(R.string.noWalletsAdded));
            activity.dialog.dismiss();
        }
    }
    private static class GetRatesHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        private GetRatesHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if (activity != null) {
                int onlineMode = msg.arg1;
                if(onlineMode==1) activity.dogeRatesObject.getCurrentRefreshTime();
                else if(onlineMode==0){
                    activity.dogeRatesObject.getRecentRefreshTime();
                    activity.makeSnackbar(activity.getString(R.string.connectionErrorText));
                    activity.dialog.dismiss();
                }
                activity.updateRatesInView(); //insert updated rates to layout
                activity.checkTrendColor(activity.dogeRatesObject.hourChangeRate, activity.hourChangeTextView);
                activity.checkTrendColor(activity.dogeRatesObject.dailyChangeRate, activity.dailyChangeTextView);
                activity.checkTrendColor(activity.dogeRatesObject.weeklyChangeRate, activity.weeklyChangeTextView);
            }
        }
    }

    private void startup_refresh(){
        Boolean appStartedNow = false;
        Bundle appLaunchedFeedback = getIntent().getExtras();
        if (appLaunchedFeedback != null)
        {
            appStartedNow = appLaunchedFeedback.getBoolean("appLaunched");
        }
        boolean auto_wallets_refresh = spref.getBoolean("wallets_auto_refresh", false);
        if(auto_wallets_refresh && isNetworkAvailable() && !startupAutoRefreshDone && appStartedNow) {
            //Refresh exchange rates every startup
            launchRefreshBalanceProcess();
            startupAutoRefreshDone = true;
        } else {
            readDataFromOffline();
        }
    }
    private void readDataFromOffline(){
        dogeRatesObject.readRatesFromOffline();
        updateRatesInView();
        String allWalletsBalanceString = String.valueOf(walletMemoryObject.calculateAllWalletsBalance());
        allWalletsBalanceTextView.setText( allWalletsBalanceString + " Đ = " + getFiatBalanceOffline());
    }

    private void checkAllExchangeTrendColors(){
        checkTrendColor(dogeRatesObject.hourChangeRate, hourChangeTextView);
        checkTrendColor(dogeRatesObject.dailyChangeRate, dailyChangeTextView);
        checkTrendColor(dogeRatesObject.weeklyChangeRate, weeklyChangeTextView);
    }
    private void checkTrendColor(String percentRate, TextView percentRateTextView){
        boolean useColorTrendsSetting = spref.getBoolean("arrow_or_color", false);
        if(percentRate == null){
            percentRateTextView.setTextColor(defaultTextColor);
            return;
        }
        if(useColorTrendsSetting) {
            if (percentRate.contains("-")) percentRateTextView.setTextColor(Color.RED);
            else percentRateTextView.setTextColor(Color.GREEN);
        } else {
            percentRateTextView.setTextColor(defaultTextColor);
        }
    }
    private void checkLogoSetting(){
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", true);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }
    private void checkNightModeSetting(){
        boolean useNightModeSetting = spref.getBoolean("nightMode", false);
        if(useNightModeSetting){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    private void getTextViews(){
        dogeRatesTextView = findViewById(R.id.doge_rate);
        hourChangeTextView = findViewById(R.id.hour_change);
        dailyChangeTextView = findViewById(R.id.daily_change);
        weeklyChangeTextView = findViewById(R.id.weekly_change);
        marketCapTextView = findViewById(R.id.market_cap);
        volumeTextView = findViewById(R.id.volume);
        totalSupplyTextView = findViewById(R.id.total_supply);
        lastUpdateTextView = findViewById(R.id.last_update);
        allWalletsBalanceTextView = findViewById(R.id.textView8);
    }
    private void updateRatesInView() {
        try {
            dogeRatesObject.saveRatesToOffline();
            dogeRatesObject.makeCommasOnRates(); //we add spaces to total supply, volume and market cap to make it clearly
            String fiatSymbol = dogeRatesObject.getFiatSymbol();

            dogeRatesTextView.setText("1Đ = " + dogeRatesObject.dogeFiatRate + " " + fiatSymbol);
            hourChangeTextView.setText(getString(R.string.hourText, dogeRatesObject.hourChangeRate, "%"));
            dailyChangeTextView.setText(getString(R.string.h24Text, dogeRatesObject.dailyChangeRate, "%"));
            weeklyChangeTextView.setText(getString(R.string.day7Text, dogeRatesObject.weeklyChangeRate, "%"));
            marketCapTextView.setText(getString(R.string.marketCapText, dogeRatesObject.marketCapRate, fiatSymbol));
            volumeTextView.setText(getString(R.string.volume24hText, dogeRatesObject.volumeRate, fiatSymbol));
            totalSupplyTextView.setText(getString(R.string.totalSupplyText, dogeRatesObject.totalSupplyRate, getString(R.string.dogecoinSymbolText)));
            lastUpdateTextView.setText(getString(R.string.lastUpdateText, dogeRatesObject.lastRefreshRate));

        } catch(NullPointerException e ){
            String errorText = getString(R.string.errorText);
            dogeRatesTextView.setText("1Đ = " + errorText);
            hourChangeTextView.setText(getString(R.string.hourText, errorText, ""));
            dailyChangeTextView.setText(getString(R.string.h24Text, errorText, ""));
            weeklyChangeTextView.setText(getString(R.string.day7Text, errorText, ""));
            marketCapTextView.setText(getString(R.string.marketCapText, errorText, ""));
            volumeTextView.setText(getString(R.string.volume24hText, errorText, ""));
            totalSupplyTextView.setText(getString(R.string.totalSupplyText, errorText, ""));
            lastUpdateTextView.setText(getString(R.string.lastUpdateText, errorText));
        }
    }
    private String getFiatBalance(){
        Float fiatBalance = walletMemoryObject.allWalletsBalance * Float.parseFloat(dogeRatesObject.dogeFiatRate);
        String fiatSymbol = dogeRatesObject.getFiatSymbol();
        return cutDecimalPlacesToTwo(String.valueOf(fiatBalance)) + " " + fiatSymbol;
    }
    private String getFiatBalanceOffline(){
        Float fiatBalance = walletMemoryObject.calculateAllWalletsBalance() * Float.parseFloat(dogeRatesObject.dogeFiatRate);
        cutDecimalPlacesToTwo(String.valueOf(fiatBalance));
        String fiatSymbol = dogeRatesObject.getFiatSymbol();
        return cutDecimalPlacesToTwo(String.valueOf(fiatBalance))  + " " + fiatSymbol;
    }
    private String cutDecimalPlacesToTwo(String FiatBalance){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        DecimalFormat df = new DecimalFormat("#.##");
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        FiatBalance = df.format(Float.parseFloat(FiatBalance));
        return FiatBalance;
    }

    //if we want to scan, we go to wallet_qr_read
    private void scanQrCode() {
        ActivityCompat.requestPermissions(MainActivity.this,
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
                    i.putExtra("quickScan", true);
                    startActivity(i);
                } else {
                    makeSnackbar(getString(R.string.grantPermissionText));
                }
            }
        }
    }
    private void checkIfQuickScanDone(){
        //If we use qr code reader, we insert here scanned address
        Bundle qrReaderMessage = getIntent().getExtras();
        if(qrReaderMessage!=null) {
            if(qrReaderMessage.getInt("readed_qr_code")==1) {
                String scannedAddress = qrReaderMessage.getString("wallet_address");
                if(wallet_verify.validateDogecoinAddress(scannedAddress)) {
                    walletMemoryObject.quickScanBalance(scannedAddress);
                    quickscandialog = new ProgressDialog(MainActivity.this);
                    quickscandialog.setCancelable(false);
                    quickscandialog.setTitle(getString(R.string.checking_balance));
                    quickscandialog.setMessage(getString(R.string.scanned_address) + scannedAddress);
                    quickscandialog.show();
                    scanned_QRcodeAddress = scannedAddress;
                } else {
                    makeSnackbar(getString(R.string.no_dogecoin_address));
                }
            }
            getIntent().removeExtra("readed_qr_code");
        }
    }
    private void buildQuickScanDoneDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
        dialogBuilder.setTitle(getString(R.string.scanned_wallet_balance));
        dialogBuilder.setMessage(walletMemoryObject.WALLET_BALANCE + " Đ");

        dialogBuilder.setPositiveButton(getString(R.string.add_to_my_wallets), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(getApplicationContext(), wallet_add.class);
                i.putExtra("wallet_address", scanned_QRcodeAddress); //show wallet_view what wallet I wanna see
                i.putExtra("readed_qr_code", 1); //show wallet_view what wallet I wanna see
                startActivity(i);
                finish();
            }
        });

        AlertDialog alert = dialogBuilder.create();
        alert.show();
    }

    private void rateAppReminder(){
        AppRate.with(this)
                .setInstallDays(1) // default 10, 0 means install day.
                .setLaunchTimes(10) // default 10
                .setRemindInterval(2) // default 1
                .setShowLaterButton(true) // default true
                .setDebug(false) // default false
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
    }
    private void checkFirstRun() {
        final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            readDataFromOffline();
            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {
            launchRefreshBalanceProcess();
            if(!isNetworkAvailable()) firstNoInternetConnectionAlert();
        } else if (currentVersionCode > savedVersionCode) {

            // TODO This is an upgrade
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
    }
    private void firstNoInternetConnectionAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setCancelable(true);
        builder.setTitle(getString(R.string.noInternetErrorTitle));

        builder.setMessage(getString(R.string.noInternetErrorMessege));
        builder.setPositiveButton(getString(R.string.confirmText),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        wifi.setWifiEnabled(true); // true or false to activate/deactivate wifi
                    }
                });
        builder.setNegativeButton(getString(R.string.cancelText), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                makeSnackbar(getString(R.string.noInternetErrorTitle));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
