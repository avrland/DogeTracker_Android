package kowoof.dogetracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    ProgressDialog dialog;
    doge_rates current_doge_rates = new doge_rates();
    Handler myHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DogeTracker");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        myHandler = new Handler();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
//        refresh_rates(); //TODO AUTO REFRESH with every startup
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Hamburger menu selection segment
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
            //finish();
        } else if (id == R.id.nav_gallery) {
            make_toast("Not implemented yet.");
        } else if (id == R.id.nav_slideshow) { //redirect to dogecin reddit
            Uri uri = Uri.parse("https://www.reddit.com/r/dogecoin/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            make_toast("Not implemented yet.");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public void refresh_button(View view) {
            refresh_rates();
    }
    public void refresh_rates(){
         //Getting current date and time
        DateFormat df = new SimpleDateFormat("d MMM yyyy, HH:mm:ss");
        String date = df.format(Calendar.getInstance().getTime());
        //Getting current dogecoin rates from coinmarketcap
        current_doge_rates.get_rates(this);
//        if(current_doge_rates.doge_rate == null){
//            current_doge_rates.get_rates(this);
//            current_doge_rates.get_rates(this);
//        }

        //Finding textView items from layout
        TextView doge_rates_text = (TextView)findViewById(R.id.doge_rate);
        TextView hour_change_text = (TextView)findViewById(R.id.hour_change);
        TextView daily_change_text = (TextView)findViewById(R.id.daily_change);
        TextView weekly_change_text = (TextView)findViewById(R.id.weekly_change);
        TextView market_cap_text = (TextView)findViewById(R.id.market_cap);
        TextView volume_text = (TextView)findViewById(R.id.volume);
        TextView total_supply_text = (TextView)findViewById(R.id.total_supply);
        TextView last_update_text = (TextView)findViewById(R.id.last_update);

        doge_rates_text.setText("1Đ = " + current_doge_rates.doge_rate + "$");
        hour_change_text.setText("1h: " + current_doge_rates.hour_change + "%");
        daily_change_text.setText("24h: " + current_doge_rates.daily_change + "%");
        weekly_change_text.setText("7d: " + current_doge_rates.weekly_change + "%");
        market_cap_text.setText("Market cap: " + current_doge_rates.market_cap + "$");
        volume_text.setText("Volume 24h: " + current_doge_rates.volume + "$");
        total_supply_text.setText("Total supply: " + current_doge_rates.total_supply + "Đ");
        last_update_text.setText("Last update: " + date);
    }
    //toast function to get it a little bit shorter
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        CharSequence text = messege_toast;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

    }


    //TODO add getting exchange rates implementation
    //TODO add regular rates refresh feature, with button too
}
