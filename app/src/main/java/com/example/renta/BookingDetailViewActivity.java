package com.example.renta;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingDetailViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail_view);

        Booking booking = (Booking) getIntent().getSerializableExtra("booking");
        if (booking == null) {
            Toast.makeText(this, "Error: Booking data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize views
        TextView carName = findViewById(R.id.detail_car_name);
        TextView bookingId = findViewById(R.id.detail_booking_id);
        TextView status = findViewById(R.id.detail_status);
        TextView dates = findViewById(R.id.detail_dates);
        TextView paymentMethod = findViewById(R.id.detail_payment_method);
        TextView downpayment = findViewById(R.id.detail_downpayment);
        TextView totalCost = findViewById(R.id.detail_total_cost);
        TextView customerName = findViewById(R.id.detail_customer_name);
        TextView phone = findViewById(R.id.detail_phone);
        TextView license = findViewById(R.id.detail_license);
        TextView address = findViewById(R.id.detail_address);
        MaterialButton cancelBtn = findViewById(R.id.cancel_booking_btn);

        // Bind data
        carName.setText(booking.getCarName());
        bookingId.setText(getString(R.string.booking_id_format, 
                booking.getBookingId() != null ? booking.getBookingId() : "N/A"));
        
        String statusText = booking.getStatus() != null ? booking.getStatus() : "Pending";
        status.setText(statusText);
        
        cancelBtn.setVisibility(View.GONE);
        if (statusText.equalsIgnoreCase("Confirmed")) {
            status.setTextColor(ContextCompat.getColor(this, R.color.available_green));
        } else if (statusText.equalsIgnoreCase("Completed")) {
            status.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            cancelBtn.setVisibility(View.VISIBLE);
            cancelBtn.setText(R.string.remove);
        } else if (statusText.equalsIgnoreCase("Cancelled")) {
            status.setTextColor(ContextCompat.getColor(this, R.color.error_red));
            cancelBtn.setVisibility(View.VISIBLE);
            cancelBtn.setText(R.string.remove);
        } else {
            status.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
            if (statusText.equalsIgnoreCase("Pending")) {
                cancelBtn.setVisibility(View.VISIBLE);
                cancelBtn.setText(R.string.cancel_action);
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        dates.setText(getString(R.string.date_range_format, 
                sdf.format(new Date(booking.getStartDate())), 
                sdf.format(new Date(booking.getEndDate()))));
        
        paymentMethod.setText(booking.getPaymentMethod());
        downpayment.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getDownPayment()));
        totalCost.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getTotalCost()));
        
        customerName.setText(booking.getCustomerName());
        phone.setText(booking.getPhoneNumber());
        license.setText(getString(R.string.license_format, booking.getLicenseNumber()));
        address.setText(booking.getAddress());

        cancelBtn.setOnClickListener(v -> {
            String currentStatus = booking.getStatus() != null ? booking.getStatus() : "Pending";
            boolean isRemoval = "Cancelled".equals(currentStatus) || "Completed".equals(currentStatus);
            String actionTitle = isRemoval ? getString(R.string.remove) : getString(R.string.cancel_action);
            String message = isRemoval ? "Remove this record from your history?" : "Are you sure you want to cancel this booking?";

            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(actionTitle + " Booking")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> {
                    com.google.firebase.database.DatabaseReference ref = FirebaseConfig.getDatabase()
                            .getReference("bookings").child(booking.getBookingId());
                    
                    if (isRemoval) {
                        ref.removeValue().addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Record removed", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    } else {
                        ref.child("status").setValue("Cancelled").addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Booking cancelled", Toast.LENGTH_SHORT).show();
                            saveNotification(booking.getUserId(), "Booking Cancelled", 
                                    "You have cancelled your booking for " + booking.getCarName());
                            finish();
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
        });
    }

    private void saveNotification(String userId, String title, String message) {
        if (userId == null) return;
        com.google.firebase.database.DatabaseReference notifRef = FirebaseConfig.getDatabase().getReference("notifications").child(userId);
        String id = notifRef.push().getKey();
        if (id != null) {
            Notification n = new Notification(id, userId, title, message, System.currentTimeMillis());
            notifRef.child(id).setValue(n);
        }
    }
}
