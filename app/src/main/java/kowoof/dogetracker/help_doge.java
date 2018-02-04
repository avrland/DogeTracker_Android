package kowoof.dogetracker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

public class help_doge extends DrawerActivity {

    private FloatingActionMenu float_menu;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private FloatingActionButton fab3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_doge);
//        float_menu = (FloatingActionMenu) findViewById(R.id.floatingMenu);
//        fab1 = (FloatingActionButton) findViewById(R.id.add_virtual_wallet);
//        fab2 = (FloatingActionButton) findViewById(R.id.add_real_wallet);
//        float_menu.setClosedOnTouchOutside(true);
//        fab1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                    make_toast("Add virtual");
//            }
//        });
//        fab2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                make_toast("Add real");
//            }
//        });
    }




}
