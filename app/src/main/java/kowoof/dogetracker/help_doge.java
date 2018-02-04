package kowoof.dogetracker;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

public class help_doge extends DrawerActivity {
    wallet_verify validation = new wallet_verify();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_doge);
    }


    public void suchverify(View view) {
        if(validation.validateDogecoinAddress("DN5KA53TqfZBWB64gNQNZKtjkBahskZXPy")==true){
            make_toast("True");
        } else make_toast("false");
    }
}
