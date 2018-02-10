package kowoof.dogetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends DrawerActivity {

    //We create doge_rates object and handler to make getting exchange rates wow
    doge_rates current_doge_rates;
    Handler handler = new Handler();

    TextView doge_rates_text, hour_change_text, daily_change_text,
            weekly_change_text, market_cap_text, volume_text,
            total_supply_text, last_update_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        doge_rates_text = findViewById(R.id.doge_rate);
        hour_change_text = findViewById(R.id.hour_change);
        daily_change_text = findViewById(R.id.daily_change);
        weekly_change_text = findViewById(R.id.weekly_change);
        market_cap_text = findViewById(R.id.market_cap);
        volume_text = findViewById(R.id.volume);
        total_supply_text = findViewById(R.id.total_supply);
        last_update_text = findViewById(R.id.last_update);

        //We create handler to wait for get exchange rates
        handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                if(msg.arg1==1)      current_doge_rates.new_refresh_time(); //if we're online, we insert current time
                else if(msg.arg1==2) current_doge_rates.offline_refresh_time(); //if we're offline, we instert last update time
                update_rates(); //insert updated rates to layout
                green_or_red(current_doge_rates.hour_change, hour_change_text);
                green_or_red(current_doge_rates.daily_change, daily_change_text);
                green_or_red(current_doge_rates.weekly_change, weekly_change_text);
            }

        };
        current_doge_rates = new doge_rates(this);
        //Refresh exchange rates every startup
        refresh_rates();
    }

    //we check and apply settings here
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean test = spref.getBoolean("dt_logo", false);
        ImageView logo = findViewById(R.id.imageView);
        if(test == false) logo.setVisibility(View.INVISIBLE);
        else logo.setVisibility(View.VISIBLE);
        green_or_red(current_doge_rates.hour_change, hour_change_text);
        green_or_red(current_doge_rates.daily_change, daily_change_text);
        green_or_red(current_doge_rates.weekly_change, weekly_change_text);
    }


    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling get_rates from doge_rates class
    //Update_rates - update rates in view
    public void refresh_button(View view) {
            refresh_rates();
    }
    public void refresh_rates(){
        //Getting current dogecoin rates from coinmarketcap
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        current_doge_rates.get_rates(handler, sp.getString("fiat_list","USD"));
    }
    void update_rates() {
        current_doge_rates.save_rates_to_offline();
        current_doge_rates.rates_with_commas(); //we add spaces to total supply, volume and market cap to make it clearly
        doge_rates_text.setText("1Đ = " + current_doge_rates.doge_rate + "$");
        hour_change_text.setText("1h: " + current_doge_rates.hour_change + "%");
        daily_change_text.setText("24h: " + current_doge_rates.daily_change + "%");
        weekly_change_text.setText("7d: " + current_doge_rates.weekly_change + "%");
        market_cap_text.setText("Market cap: " + current_doge_rates.market_cap + " $");
        volume_text.setText("Volume 24h: " + current_doge_rates.volume + " $");
        total_supply_text.setText("Total supply: " + current_doge_rates.total_supply + " Đ");
        last_update_text.setText("Last update: " + current_doge_rates.last_refresh);
    }
    //Check if percent rate are collapsing or raising
    public void green_or_red(String percent_rate, TextView percent_rate_textview){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean check_setting = spref.getBoolean("arrow_or_color", false);
        if(percent_rate == null){
            percent_rate_textview.setTextColor(Color.rgb(0x75, 0x75, 0x75));
            return;
        }
        if(check_setting == true) {
            if (percent_rate.contains("-")) percent_rate_textview.setTextColor(Color.RED);
            else percent_rate_textview.setTextColor(Color.GREEN);
        } else {
            //default theme color, so hard to find it directly lol
            percent_rate_textview.setTextColor(Color.rgb(0x75, 0x75, 0x75));
        }
    }

    public void TEST(View view) {
        make_toast("TEST");
    }
}
