package com.example.renta;

import com.google.firebase.database.FirebaseDatabase;

public class FirebaseConfig {
    public static final String PRIMARY_URL = "https://renta-car-30a6f-default-rtdb.asia-southeast1.firebasedatabase.app";
    public static final String ALT_URL = "https://renta-car-30a6f.asia-southeast1.firebasedatabase.app";

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance(PRIMARY_URL);
    }
}
