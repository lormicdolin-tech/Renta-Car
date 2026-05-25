package com.example.renta;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private static final String PREF_NAME = "renta_bookings";
    private static final String KEY_BOOKINGS = "bookings_list";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public BookingManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addBooking(Booking booking) {
        List<Booking> bookings = getBookings();
        bookings.add(booking);
        saveBookings(bookings);
    }

    public List<Booking> getBookings() {
        String json = sharedPreferences.getString(KEY_BOOKINGS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Booking>>() {}.getType();
        return gson.fromJson(json, type);
    }

    private void saveBookings(List<Booking> bookings) {
        String json = gson.toJson(bookings);
        sharedPreferences.edit().putString(KEY_BOOKINGS, json).apply();
    }
}
