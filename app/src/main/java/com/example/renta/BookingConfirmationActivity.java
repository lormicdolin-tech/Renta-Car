package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class BookingConfirmationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_confirmation);

        String bookingId = getIntent().getStringExtra("booking_id");
        String email = getIntent().getStringExtra("email");

        TextView idText = findViewById(R.id.conf_booking_id);
        TextView messageText = findViewById(R.id.conf_message);
        MaterialButton viewBookingBtn = findViewById(R.id.view_booking_btn);

        if (bookingId != null) {
            idText.setText(bookingId);
        }

        if (email != null && !email.isEmpty()) {
            messageText.setText(getString(R.string.booking_details_sent, email));
        } else {
            messageText.setText(getString(R.string.booking_details_history));
        }

        viewBookingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("navigate_to", "bookings");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
