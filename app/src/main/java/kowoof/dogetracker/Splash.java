package kowoof.dogetracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class Splash extends AppCompatActivity {
    Animation anim;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSplashSettings();
    }

    private void checkSplashSettings(){
        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(Splash.this);
        boolean useSplashScreen = spref.getBoolean("splash", true);
        if(useSplashScreen){
            setContentView(R.layout.activity_splash);
            imageView= findViewById(R.id.imageView2); // Declare an imageView to show the animation.
            anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in); // Create the animation.
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
                    i.putExtra("appLaunched", true);
                    startActivity(i);
                    finish();
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            imageView.startAnimation(anim);
        } else {
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("appLaunched", true);
            startActivity(i);
            finish();
        }
    }
}
