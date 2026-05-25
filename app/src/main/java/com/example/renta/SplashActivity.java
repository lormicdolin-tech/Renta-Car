package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

/**
 * SplashActivity: The entry point of the application.
 * Displays the app logo for 2 seconds and determines whether to send the user
 * to the Login screen or the Home screen based on their authentication status.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Delay for 2 seconds (2000 milliseconds)
        new Handler().postDelayed(() -> {
            // Always go to Login screen for now to ensure local user flow
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            // Close the SplashActivity so it's not in the back stack
            finish();
        }, 2000);
    }
}
