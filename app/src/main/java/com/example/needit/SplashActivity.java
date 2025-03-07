package com.example.needit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3000; // Splash screen timer (2 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Make sure you have a layout for the splash screen

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the IntroActivity after the splash screen duration
                Intent mainIntent = new Intent(SplashActivity.this, IntroActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}
