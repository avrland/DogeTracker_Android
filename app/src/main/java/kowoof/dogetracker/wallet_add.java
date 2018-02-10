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

public class wallet_add extends AppCompatActivity {

    //We create object to save stuff to memory
    wallet_memory wallet_memory_handler;
    //We create strings to store address and name before adding
    String added_wallet_name, added_wallet_address;
    //We create handler to react after getting first time balance
    Handler handler = new Handler();
    //We create object to get first time balance (next time we'll do it in wallet_list and wallet_view)
    wallet_balance current_wallet_balance = new wallet_balance();
    //We create object from class to verify if Dogecoin address is valid
    wallet_verify wow_verify = new wallet_verify();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add real wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        EditText wallet_name_editText = findViewById(R.id.editText2);
        wallet_name_editText.requestFocus();
        wallet_memory_handler = new wallet_memory(getApplicationContext());
        FloatingActionButton fab = findViewById(R.id.fab);
        //We get here wallet address and name, and save it to SharedPref
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //TODO we should check here if dogecoin wallet address is valid (or into wallet_view?)
                EditText wallet_name_editText = findViewById(R.id.editText2);
                added_wallet_name = wallet_name_editText.getText().toString();
                EditText wallet_address_editText = findViewById(R.id.editText);
                added_wallet_address = wallet_address_editText.getText().toString();
                if(added_wallet_name.trim().length() == 0) added_wallet_name = added_wallet_address;
                if(wow_verify.validateDogecoinAddress(added_wallet_address)==true){
                    current_wallet_balance.get_wallet_balance(wallet_add.this, handler, added_wallet_address);
                } else {
                    Snackbar mySnackbar = Snackbar.make(getWindow().getDecorView(),"Invalid address.", Snackbar.LENGTH_SHORT);
                    mySnackbar.show();
                }
            }
        });

        //If we use qr code reader, we insert here scanned address
        Bundle b = getIntent().getExtras();
        if(b!=null) {
            int readed_qr = b.getInt("readed_qr_code");
            if (readed_qr==1)
            {
                added_wallet_address = b.getString("wallet_address");
                EditText editText = findViewById(R.id.editText);
                editText.setText(added_wallet_address);
            }
        }


        //We create handler to wait for get exchange rates
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                try {
                    wallet_memory_handler.add_to_wallets_with_balance(added_wallet_name, added_wallet_address, current_wallet_balance.balance);
                    Intent i = new Intent(getApplicationContext(), wallet_list.class);
                    i.putExtra("added_wallet", 1);
                    startActivity(i);
                    finish();
                } catch (JSONException e) {
                    make_toast("Connection error.");
                }
            }

        };
    }

    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean test = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(test == false) logo.setVisibility(View.INVISIBLE);
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
    public void paste_wallet_address(View view) {
        ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData abc = clipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String pasted_wallet_address = item.getText().toString();
        //todo check if there is anything in clipbaord,
        EditText editText = findViewById(R.id.editText);
        editText.setText(pasted_wallet_address);

        ConstraintLayout layout = findViewById(R.id.snackbar_layout_add);
        Snackbar snackbar = Snackbar
                .make(layout, "Address pasted.", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

    //if we want to scan, we go to wallet_qr_read
    public void scan_qr_code(View view) {
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
                    Toast.makeText(wallet_add.this, "Grant permission to camera to read qr code.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    //toast function to get it a little bit shorter
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context,  messege_toast, duration);
        toast.show();
    }

//    //Preparing json object to save
//    public void test_button(View view) throws JSONException  {
//        EditText wallet_name_editText = findViewById(R.id.editText2);
//        added_wallet_name = wallet_name_editText.getText().toString();
//
//        EditText wallet_address_editText = findViewById(R.id.editText);
//        added_wallet_address = wallet_address_editText.getText().toString();
//        wallet_memory_handler.add_to_wallets(added_wallet_name, added_wallet_address);
//    }
//
//    public void test_button2(View view) {
//        wallet_memory_handler.current_context = getApplicationContext();
//        wallet_memory_handler.read_all_wallets();
//    }
}
