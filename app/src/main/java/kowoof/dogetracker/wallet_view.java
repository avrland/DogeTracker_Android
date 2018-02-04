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



public class wallet_view extends DrawerActivity {

    static String qr_reading_address = "https://dogechain.info/api/v1/address/qrcode/";

    String wallet_name, wallet_address;
    int wallet_id;
    wallet_balance current_wallet_balance = new wallet_balance();
    wallet_memory wallet_memory_handler;
    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Get feedback from wallet_qr_read activity
        Bundle b = getIntent().getExtras();
        if (b != null)
        {
            wallet_name = b.getString("wallet_name");
            wallet_address = b.getString("wallet_address");
            wallet_id = b.getInt("wallet_id");
        }
        //Prepare view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Wallet view");
        toolbar.setSubtitle("");

        TextView wallet_name_text = findViewById(R.id.wallet_name);
        TextView wallet_address_text = findViewById(R.id.wallet_address);
        wallet_name_text.setText(wallet_name);
        if(wallet_name == wallet_address) wallet_name_text.setText("");
        wallet_address_text.setText(wallet_address);
        wallet_memory_handler = new wallet_memory(getApplicationContext());
        //Set button for deleting wallet (just from viewer, not really lol)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(wallet_view.this);
                builder.setCancelable(true);
                builder.setTitle("Are you sure?");
                builder.setMessage("Remove wallet: " + wallet_name);
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    wallet_memory_handler.remove_wallet(wallet_id);
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
        //We create handler to wait for get exchange rates
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                show_balance();
            }
        };

        //Get single wallet balance
        get_balance();

        //QR code download&set section
        ImageView qrcode = findViewById(R.id.imageView2);
        Picasso.with(this).load(qr_reading_address + wallet_address).into(qrcode);
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
        boolean test = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(test == false) logo.setVisibility(View.INVISIBLE);
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
    public void get_balance(){
        current_wallet_balance.get_wallet_balance(this, handler, wallet_address);
    }
    public void show_balance(){
        TextView wallet_balance_text = findViewById(R.id.balance);
        wallet_balance_text.setText(current_wallet_balance.balance + " Đ");
        balance_in_dollars();
    }
    public void balance_in_dollars(){
        doge_rates get_doge_dollar_rate = new doge_rates(getApplicationContext());
        get_doge_dollar_rate.read_rates_from_offline();
        float dolar_doge_f = Float.parseFloat(get_doge_dollar_rate.doge_rate);
        float balance_f = Float.parseFloat(current_wallet_balance.balance);
        float total_dollar_balance = dolar_doge_f * balance_f;
        String dollar_doge_s = Float.toString(total_dollar_balance);
        TextView wallet_balance_text = findViewById(R.id.doge_in_dollars);
        wallet_balance_text.setText(dollar_doge_s + " $");
    }
    //Copy wallet address by clicking qr code
    public void copy_wallet_address(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", wallet_address);
        clipboard.setPrimaryClip(clip);

        ConstraintLayout layout = findViewById(R.id.snackbar_layout_view);
        Snackbar snackbar = Snackbar
                .make(layout, "Address copied to clipboard.", Snackbar.LENGTH_SHORT);
        snackbar.show();
    }

}

