package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.github.florent37.viewtooltip.ViewTooltip;

import org.json.JSONException;


public class wallet_add_virtual extends AppCompatActivity {

    //We create object to save stuff to memory
    private wallet_memory walletMemoryObject;

    private String addedVirtualWalletName, addedVirtualWalletBalance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add_virtual);
        setToolbar();
        addWalletFabHandler();
        focusOnNameEditText();

        walletMemoryObject = new wallet_memory(getApplicationContext());
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        checkDTLogoSetting();
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

    private void setToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.addVirtualWalletText));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    private void checkDTLogoSetting(){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean useBackgroundLogoSetting = spref.getBoolean("dt_logo", true);
        ImageView logo = findViewById(R.id.imageView);
        if(!useBackgroundLogoSetting) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
    }
    public void addWalletFabHandler(){
        FloatingActionButton addWalletFab = findViewById(R.id.fab);
        //We get here wallet address and name, and save it to SharedPref
        addWalletFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText walletNameEditText = findViewById(R.id.editText10);
                addedVirtualWalletName = walletNameEditText.getText().toString();
                walletNameEditText.requestFocus();
                EditText virtualWalletBalance = findViewById(R.id.editText11);
                addedVirtualWalletBalance = virtualWalletBalance.getText().toString();
                ifNameEmptyAddAddressAsName();
                try {
                    walletMemoryObject.addToWalletsWithBalance(addedVirtualWalletName, "Virtual", addedVirtualWalletBalance);
                    Intent i = new Intent(getApplicationContext(), wallet_list.class);
                    i.putExtra("added_wallet", 1);
                    startActivity(i);
                    finish();
                } catch (JSONException e){
                    makeSnackbar("Something went wrong :(");
                }
            }
        });
    }
    private void ifNameEmptyAddAddressAsName(){
        if(addedVirtualWalletName.trim().length() == 0) addedVirtualWalletName = "Virtual wallet";
    }
    private void focusOnNameEditText(){
        EditText walletNameEditText = findViewById(R.id.editText10);
        walletNameEditText.requestFocus();
    }
    //toast function to get it a little bit shorter
    public void makeSnackbar(String snackbar_message){
        Snackbar snackbar = Snackbar
                .make(getWindow().getDecorView(), snackbar_message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
}
