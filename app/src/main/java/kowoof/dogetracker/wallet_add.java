package kowoof.dogetracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class wallet_add extends AppCompatActivity {
    //wallet added_wallet = new wallet();
    JSONObject jsonObj;
    wallet_memory wallet_memory_handler;
    String added_wallet_name, added_wallet_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_add);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add wallet");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        EditText wallet_name_editText = findViewById(R.id.editText2);
        wallet_name_editText.requestFocus();
        wallet_memory_handler = new wallet_memory(getApplicationContext());
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText wallet_name_editText = findViewById(R.id.editText2);
                added_wallet_name = wallet_name_editText.getText().toString();
                EditText wallet_address_editText = findViewById(R.id.editText);
                added_wallet_address = wallet_address_editText.getText().toString();
                try {
                    wallet_memory_handler.add_to_wallets(added_wallet_name, added_wallet_address);
                } catch (JSONException e) {

                }
                //TODO add address checking here
                Intent i = new Intent(getApplicationContext(), wallet_list.class);
                startActivity(i);
                finish();
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
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void paste_wallet_address(View view) {
        ClipboardManager clipboard=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData abc = clipboard.getPrimaryClip();
        ClipData.Item item = abc.getItemAt(0);
        String pasted_wallet_address = item.getText().toString();
        EditText editText = findViewById(R.id.editText);
        editText.setText(pasted_wallet_address);
        make_toast("Pasted.");
    }

    public void scan_qr_code(View view) throws JSONException{
        make_toast("Not implemented.");
    }

    //toast function to get it a little bit shorter
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        CharSequence text = messege_toast;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
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
