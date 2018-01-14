package kowoof.dogetracker;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class wallet_view extends AppCompatActivity {

    String wallet_name, wallet_address;
    wallet_balance current_wallet_balance = new wallet_balance();

    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Get feedback from wallet_list activity
        Bundle b = getIntent().getExtras();
        int id = b.getInt("id");
        wallet_name = b.getString("wallet_name");
        wallet_address = b.getString("wallet_address");

        //Prepare view
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Wallet view");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView wallet_name_text = findViewById(R.id.wallet_name);
        TextView wallet_address_text = findViewById(R.id.wallet_address);
        wallet_name_text.setText(wallet_name);
        wallet_address_text.setText(wallet_address);

        //Set button for deleting wallet (just from viewer, not really lol)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(wallet_view.this);
                builder.setCancelable(true);
                builder.setTitle("Are you sure?");
                builder.setMessage("Remove wallet: wallet name");
                builder.setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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

        get_balance();

        //QR code download&set section
        ImageView qrcode = findViewById(R.id.imageView2);
        Picasso.with(this).load("https://dogechain.info/api/v1/address/qrcode/" + wallet_address).into(qrcode);
    }

    // Letting come back home
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
    public void get_balance(){
        current_wallet_balance.get_wallet_balance(this, handler, wallet_address);
    }
    public void show_balance(){
        TextView wallet_balance_text = findViewById(R.id.balance);
        wallet_balance_text.setText(current_wallet_balance.balance + " Đ");
    }
    //Copy wallet address by clicking qr code
    public void copy_wallet_address(View view) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", wallet_address);
        clipboard.setPrimaryClip(clip);
        make_toast("Address copied to clipboard.");
    }

    // Last but not least, useful stuff to make app working
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        CharSequence text = messege_toast;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}

