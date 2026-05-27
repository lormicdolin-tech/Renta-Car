package com.example.renta;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingManager {
    private static final String TAG = "RentA_BookingManager";
    private final DatabaseReference mDatabase;

    public BookingManager(Context context) {
        // Use explicit regional URL to avoid default US-central routing error
        mDatabase = FirebaseConfig.getDatabase().getReference().child("bookings");
        
        Log.d(TAG, "BookingManager initialized with URL: " + FirebaseConfig.PRIMARY_URL);
        
        // Monitor connection status for the regional instance
        FirebaseConfig.getDatabase().getReference(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                Log.d(TAG, "Firebase Connection Status (" + FirebaseConfig.PRIMARY_URL + "): " + (connected ? "CONNECTED" : "DISCONNECTED"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Connection monitor cancelled: " + error.getMessage());
            }
        });
    }

    public void addBooking(Booking booking, OnBookingCompleteListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = (user != null) ? user.getUid() : "anonymous_guest";

        generateBookingId(id -> {
            booking.setBookingId(id);
            booking.setUserId(currentUserId);
            
            if (booking.getStatus() == null) {
                booking.setStatus("Pending");
            }

            // Convert to Map to avoid serialization issues and ensure all fields are explicitly set
            Map<String, Object> bookingMap = new HashMap<>();
            bookingMap.put("bookingId", booking.getBookingId());
            bookingMap.put("userId", booking.getUserId());
            bookingMap.put("carName", booking.getCarName());
            bookingMap.put("carPrice", booking.getCarPrice());
            bookingMap.put("startDate", booking.getStartDate());
            bookingMap.put("endDate", booking.getEndDate());
            bookingMap.put("totalCost", booking.getTotalCost());
            bookingMap.put("downPayment", booking.getDownPayment());
            bookingMap.put("paymentMethod", booking.getPaymentMethod());
            bookingMap.put("phoneNumber", booking.getPhoneNumber());
            bookingMap.put("customerName", booking.getCustomerName());
            bookingMap.put("address", booking.getAddress());
            bookingMap.put("licenseNumber", booking.getLicenseNumber());
            bookingMap.put("status", booking.getStatus());
            bookingMap.put("guestBooking", booking.isGuestBooking());

            Log.d(TAG, "Attempting write for booking ID: " + id + " for user: " + currentUserId);

            mDatabase.child(id).setValue(bookingMap)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Booking saved successfully.");
                        
                        // Create and save a notification
                        if (user != null) {
                            saveNotification(currentUserId, "Booking Confirmed", 
                                "Your booking for " + booking.getCarName() + " has been successfully submitted and is currently pending.");
                        }
                        
                        listener.onComplete(true, id); // Return the ID on success
                    } else {
                        Exception e = task.getException();
                        String errorMsg = (e != null) ? e.getMessage() : "Unknown Database Error";
                        Log.e(TAG, "Firebase save failed: " + errorMsg);
                        listener.onComplete(false, "Database Error: " + errorMsg);
                    }
                });
        });
    }

    private void generateBookingId(final OnIdGeneratedListener listener) {
        String datePart = new java.text.SimpleDateFormat("yyyy-MMdd", java.util.Locale.getDefault()).format(new java.util.Date());
        String prefix = "TRIP-" + datePart + "-";
        
        mDatabase.orderByKey().startAt(prefix).endAt(prefix + "\uf8ff")
            .limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    int nextNum = 1;
                    if (snapshot.exists()) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String lastId = ds.getKey();
                            if (lastId != null && lastId.startsWith(prefix)) {
                                try {
                                    String numStr = lastId.substring(prefix.length());
                                    nextNum = Integer.parseInt(numStr) + 1;
                                } catch (Exception e) {
                                    Log.e(TAG, "Error parsing ID: " + lastId);
                                }
                            }
                        }
                    }
                    String newId = prefix + String.format(java.util.Locale.getDefault(), "%03d", nextNum);
                    listener.onIdGenerated(newId);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback to push key if query fails, but with prefix
                    listener.onIdGenerated(prefix + System.currentTimeMillis() % 1000);
                }
            });
    }

    private interface OnIdGeneratedListener {
        void onIdGenerated(String id);
    }

    private void saveNotification(String userId, String title, String message) {
        DatabaseReference notifRef = FirebaseConfig.getDatabase().getReference("notifications").child(userId);
        String notifId = notifRef.push().getKey();
        if (notifId != null) {
            Notification notification = new Notification(notifId, userId, title, message, System.currentTimeMillis());
            notifRef.child(notifId).setValue(notification)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Notification saved successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to save notification: " + e.getMessage()));
        }
    }

    public void getBookings(OnBookingsLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            listener.onLoaded(new ArrayList<>());
            return;
        }

        mDatabase.orderByChild("userId").equalTo(user.getUid())
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<Booking> bookings = new ArrayList<>();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        Booking booking = postSnapshot.getValue(Booking.class);
                        if (booking != null) {
                            bookings.add(booking);
                        }
                    }
                    listener.onLoaded(bookings);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Failed to load bookings: " + error.getMessage());
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
