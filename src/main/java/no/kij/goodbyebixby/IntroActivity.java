package no.kij.goodbyebixby;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by kissy on 15.04.2017.
 */

public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addSlide(AppIntroFragment.newInstance(Html.fromHtml("Goodbye<b>Bixby</b>", 0), "Tired of Bixby?\r\nWant to use that button for something else?\r\nSay Goodbye to Bixby", R.drawable.bixby, getColor(R.color.introColor)));
        addSlide(Slide.newInstance(R.layout.slide_1));
        addSlide(AppIntroFragment.newInstance("You're done!", "That's all! Now, go rebind that Bixby button to something useful!", R.drawable.checkmark, getColor(R.color.kindaBlue)));


        showSkipButton(false);
    }

    public void pressMe(View view) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        Toast.makeText(this, "Please turn on GoodbyeBixby!", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        finish();
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.preference_key), MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putBoolean("firstRun", false);
        e.apply();
        finish();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
        // Do something when the slide changes.
    }
}
