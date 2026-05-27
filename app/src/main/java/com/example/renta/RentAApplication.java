package com.example.renta;

import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;

public class RentAApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Enable disk persistence for better offline/slow-connection handling
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            // Persistence might already be initialized in some contexts
        }
    }
}
