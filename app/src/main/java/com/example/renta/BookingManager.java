package com.example.renta;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BookingManager {
    private final DatabaseReference mDatabase;

    public BookingManager(Context context) {
        mDatabase = FirebaseDatabase.getInstance().getReference().child("bookings");
    }

    public void addBooking(Booking booking, OnBookingCompleteListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (user != null) ? user.getUid() : null;

        if (currentUserId == null) {
            if (booking.isGuestBooking()) {
                currentUserId = "GUEST_USER";
                booking.setStatus("Pending [Guest]");
            } else {
                listener.onComplete(false, "User session not found. Please log in.");
                return;
            }
        }

        String bookingId = mDatabase.push().getKey();
        if (bookingId != null) {
            booking.setBookingId(bookingId);
            booking.setUserId(currentUserId);
            
            android.util.Log.d("RentA_Booking", "Saving booking: " + bookingId + " for user: " + currentUserId);

            mDatabase.child(bookingId).setValue(booking, (error, ref) -> {
                if (error == null) {
                    listener.onComplete(true, "Booking successful!");
                } else {
                    listener.onComplete(false, "Database Error: " + error.getMessage());
                }
            });
        } else {
            listener.onComplete(false, "Cloud Error: Could not generate booking ID.");
        }
    }

    public void getBookings(OnBookingsLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (user != null) ? user.getUid() : null;

        if (currentUserId == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        mDatabase.orderByChild("userId").equalTo(currentUserId)
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Booking> bookings = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Booking booking = postSnapshot.getValue(Booking.class);
                        bookings.add(booking);
                    }
                    listener.onLoaded(bookings);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    listener.onLoaded(new ArrayList<>());
                }
            });
    }

    public interface OnBookingCompleteListener {
        void onComplete(boolean success, String message);
    }

    public interface OnBookingsLoadedListener {
        void onLoaded(List<Booking> bookings);
    }
}
