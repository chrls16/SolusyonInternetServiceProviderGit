package com.example.solusyoninternetserviceprovider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth; // Add this

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // 1. CHECK IF USER IS ALREADY LOGGED IN
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra("IS_AUTO_LOGIN", true); // Pass flag to MainActivity
                startActivity(intent);
                finish();
                return;
            }

            // 2. CHECK ONBOARDING COMPLETION
            SharedPreferences preferences = getSharedPreferences("onboarding_pref", MODE_PRIVATE);
            boolean isFinished = preferences.getBoolean("isFinished", false);

            Intent intent;
            if (isFinished) {
                intent = new Intent(SplashActivity.this, WelcomeActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, OnboardingActivity.class);
            }

            startActivity(intent);
            finish();
        }, 2500);
    }
}