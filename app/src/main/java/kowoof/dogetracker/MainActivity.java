package kowoof.dogetracker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends DrawerActivity {

    //We create doge_rates object and handler to make getting exchange rates wow
    doge_rates current_doge_rates;
    Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //We create handler to wait for get exchange rates
        handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg); //don't know it's really needed now
                if(msg.arg1==1)      current_doge_rates.new_refresh_time(); //if we're online, we insert current time
                else if(msg.arg1==2) current_doge_rates.offline_refresh_time(); //if we're offline, we instert last update time
                update_rates(); //insert updated rates to layout
            }

        };
        current_doge_rates = new doge_rates(this);
        //Refresh exchange rates every startup
        refresh_rates();
    }



    //Refresh button - selects response for clicking refresh - refresh_rates
    //Refresh rates - calling get_rates from doge_rates class
    //Update_rates - update rates in view
    public void refresh_button(View view) {
            refresh_rates();
    }
    public void refresh_rates(){
        //Getting current dogecoin rates from coinmarketcap
        current_doge_rates.get_rates(handler);
    }
    void update_rates() {
        TextView doge_rates_text = findViewById(R.id.doge_rate);
        TextView hour_change_text = findViewById(R.id.hour_change);
        TextView daily_change_text = findViewById(R.id.daily_change);
        TextView weekly_change_text = findViewById(R.id.weekly_change);
        TextView market_cap_text = findViewById(R.id.market_cap);
        TextView volume_text = findViewById(R.id.volume);
        TextView total_supply_text = findViewById(R.id.total_supply);
        TextView last_update_text = findViewById(R.id.last_update);
        current_doge_rates.save_rates_to_offline();
        current_doge_rates.rates_with_commas(); //we add spaces to total supply, volume and market cap to make it clearly
        green_or_red(current_doge_rates.hour_change, hour_change_text);
        green_or_red(current_doge_rates.daily_change, daily_change_text);
        green_or_red(current_doge_rates.weekly_change, weekly_change_text);
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
        if(percent_rate.contains("-")){
            percent_rate_textview.setTextColor(Color.RED);
        } else {
            percent_rate_textview.setTextColor(Color.GREEN);
        }
    }
    //toast function to get it a little bit shorter
    public void make_toast(String messege_toast){
        Context context = getApplicationContext();
        CharSequence text = messege_toast;
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    public void TEST(View view) {
        make_toast("TEST");
    }
}
