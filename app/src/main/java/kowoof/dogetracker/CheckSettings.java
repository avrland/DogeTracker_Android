package kowoof.dogetracker;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

public class CheckSettings {
    private SharedPreferences spref;
    private boolean mUseBackgroundLogoSetting;
    private View mLogo;

    public CheckSettings(boolean useBackgroundLogoSetting, View logo){
        mUseBackgroundLogoSetting = useBackgroundLogoSetting;
        mLogo = logo;
    }
    public void checkLogoSetting(){
        if(!mUseBackgroundLogoSetting) mLogo.setVisibility(View.INVISIBLE);
        else mLogo.setVisibility(View.VISIBLE);
    }


}
