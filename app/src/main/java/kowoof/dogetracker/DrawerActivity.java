package kowoof.dogetracker;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
/**
 * Created by Marcin on 11.02.2018.
 * Copyright © 2017 Marcin Popko. All rights reserved.
 */

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected RelativeLayout fullLayout;
    protected FrameLayout frameLayout;

    @Override
    public void setContentView(int layoutResID) {
        //Prepare default drawer view section
        fullLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.activity_drawer, null);
        frameLayout = fullLayout.findViewById(R.id.drawer_frame);
        getLayoutInflater().inflate(layoutResID, frameLayout, true);
        super.setContentView(fullLayout);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("DogeTracker");
        toolbar.setSubtitle(BuildConfig.VERSION_NAME);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //End of preparing default drawer view section
    }

    //We close drawer here with pressing back button
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Options from right toolbar, I don't use it now (maybe someday)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    //Hamburger menu selection segment
//    @SuppressWarnings("StatementWithEmptyBody")
//    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent i = new Intent(getApplicationContext(), wallet_list.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_gallery) {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
        } else if (id == R.id.nav_slideshow) { //redirect to dogecin reddit
            Uri uri = Uri.parse("https://www.reddit.com/r/dogecoin/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Uri uri = Uri.parse("https://www.reddit.com/r/dogetracker/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
        else if (id == R.id.settings) {
            Intent i = new Intent(getApplicationContext(), settings_activity.class);
            startActivity(i);
            //finish();
        }
        else if (id == R.id.help_doge) {
            Intent i = new Intent(getApplicationContext(), help_doge.class);
            startActivity(i);
            finish();
        }
        else if (id == R.id.nav_backup) {
            makeSnackbar("Coming soon.");
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //toast function to get it a little bit shorter
    public void makeSnackbar(String snackbar_message){
        Snackbar snackbar = Snackbar
                .make(getWindow().getDecorView(), snackbar_message, Snackbar.LENGTH_SHORT);
        snackbar.show();
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
