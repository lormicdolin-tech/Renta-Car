package com.example.renta;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Logger;

public class RentAApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        applyDarkMode();

        try {
            // Standardizing on the confirmed regional URL
            FirebaseDatabase db = FirebaseConfig.getDatabase();
            
            // Enable debug logging to see why it stays "Offline" in Logcat
            db.setLogLevel(Logger.Level.DEBUG);
            
            // RE-ENABLE PERSISTENCE
            db.setPersistenceEnabled(true); 
            
            // Force a fresh connection attempt
            db.goOffline();
            
            // Kick-start the network connection with a small delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                db.goOnline();
                android.util.Log.d("RentA_App", "Firebase connection forced ONLINE");
            }, 1500);
            
            android.util.Log.d("RentA_App", "Firebase Regional Init: " + FirebaseConfig.PRIMARY_URL + " (Persistence ON)");
        } catch (Exception e) {
            android.util.Log.e("RentA_App", "Firebase Init Error: " + e.getMessage());
        }
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
