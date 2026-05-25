package com.example.renta;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

/**
 * UserManager: Handles local persistence of user profile data using SharedPreferences.
 * This allows the app to remember the user's name and email across sessions
 * even when offline, and provides default values for new users.
 */
public class UserManager {
    private static final String PREF_NAME = "renta_user";
    private static final String KEY_USER = "current_user";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    /**
     * Constructor: Initializes SharedPreferences and Gson for JSON serialization.
     */
    public UserManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Saves the User object as a JSON string in SharedPreferences.
     */
    public void saveUser(User user) {
        String json = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER, json).apply();
    }

    /**
     * Retrieves the saved User object.
     * Returns a default "Juan Dela Cruz" user if no data is found.
     */
    public User getUser() {
        String json = sharedPreferences.getString(KEY_USER, null);
        if (json == null) {
            // Default guest user profile
            return new User("Juan Dela Cruz", "juan@example.ph");
        }
        return gson.fromJson(json, User.class);
    }
}
