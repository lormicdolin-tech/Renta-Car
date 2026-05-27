package com.example.renta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * SplashActivity: The entry point of the application.
 * Displays the app logo for 2 seconds and determines whether to send the user
 * to the Login screen or the Home screen based on their authentication status.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_splash);

        // Delay for 2 seconds (2000 milliseconds)
        new Handler().postDelayed(() -> {
            checkAutoLogin();
        }, 2000);
    }

    private void checkAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        boolean autoLoginEnabled = prefs.getBoolean("auto_login", false);
        boolean isAdmin = prefs.getBoolean("is_admin", false);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (autoLoginEnabled && currentUser != null) {
            // Proceed to the correct screen based on stored admin status
            Intent intent;
            if (isAdmin) {
                intent = new Intent(SplashActivity.this, AdminActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, HomeActivity.class);
            }
            startActivity(intent);
        } else {
            // No auto-login or no user, go to Login
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        }
        finish();
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
