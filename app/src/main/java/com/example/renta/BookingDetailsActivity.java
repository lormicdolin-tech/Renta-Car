package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class BookingDetailsActivity extends AppCompatActivity {

    private TextInputEditText nameEdit, addressEdit, licenseEdit;
    private TextInputLayout nameLayout, addressLayout, licenseLayout;
    private Booking pendingBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_details);

        pendingBooking = (Booking) getIntent().getSerializableExtra("pending_booking");
        if (pendingBooking == null) {
            Toast.makeText(this, "Error: Booking data missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        nameEdit = findViewById(R.id.customerNameEdit);
        addressEdit = findViewById(R.id.addressEdit);
        licenseEdit = findViewById(R.id.licenseEdit);
        nameLayout = findViewById(R.id.nameLayout);
        addressLayout = findViewById(R.id.addressLayout);
        licenseLayout = findViewById(R.id.licenseLayout);
        MaterialButton completeBtn = findViewById(R.id.completeBookingBtn);

        // Pre-fill name if registered user
        if (!pendingBooking.isGuestBooking()) {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid()).child("name").get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            nameEdit.setText(snapshot.getValue(String.class));
                        }
                    });
            }
        } else {
            Toast.makeText(this, "Guest booking: Please ensure your license details are correct.", Toast.LENGTH_LONG).show();
        }

        completeBtn.setOnClickListener(v -> {
            if (validate()) {
                android.util.Log.d("RentA_Booking", "Validation passed, starting submission");
                
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                completeBtn.setEnabled(false);
                completeBtn.setText("Processing...");
                Toast.makeText(this, "Saving your booking...", Toast.LENGTH_SHORT).show();

                pendingBooking.setCustomerName(nameEdit.getText().toString().trim());
                pendingBooking.setAddress(addressEdit.getText().toString().trim());
                pendingBooking.setLicenseNumber(licenseEdit.getText().toString().trim().toUpperCase());

                // Add a local timeout handler with a more generous window (30 seconds)
                new android.os.Handler().postDelayed(() -> {
                    if (!isFinishing() && !completeBtn.isEnabled()) {
                        completeBtn.setEnabled(true);
                        completeBtn.setText("Confirm and Finalize");
                        
                        String errorMsg = "The server is taking too long to respond. ";
                        if (!isNetworkAvailable()) {
                            errorMsg += "Please check your internet connection.";
                        } else {
                            errorMsg += "Please try again or check your connection.";
                        }
                        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                        android.util.Log.e("RentA_Booking", "Firebase submission timeout after 30s");
                    }
                }, 30000); 

                new BookingManager(this).addBooking(pendingBooking, (success, message) -> {
                    if (isFinishing()) return;
                    
                    if (success) {
                        android.util.Log.d("RentA_Booking", "Booking saved successfully");
                        Toast.makeText(this, "Booking Successful! Visit office for next steps.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        android.util.Log.e("RentA_Booking", "Save failed: " + message);
                        completeBtn.setEnabled(true);
                        completeBtn.setText("Confirm and Finalize");
                        Toast.makeText(this, "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean validate() {
        boolean valid = true;
        String name = nameEdit.getText() != null ? nameEdit.getText().toString().trim() : "";
        String address = addressEdit.getText() != null ? addressEdit.getText().toString().trim() : "";
        String license = licenseEdit.getText() != null ? licenseEdit.getText().toString().trim() : "";

        if (name.isEmpty()) {
            nameLayout.setError("Full name is required");
            valid = false;
        } else {
            nameLayout.setError(null);
        }

        if (address.isEmpty()) {
            addressLayout.setError("Address is required");
            valid = false;
        } else {
            addressLayout.setError(null);
        }

        if (license.isEmpty()) {
            licenseLayout.setError("License number is required");
            valid = false;
        } else if (!license.matches("^[A-Z][0-9A-Z\\- ]{4,20}$")) {
            licenseLayout.setError("Invalid format (e.g., A01-23-456789)");
            valid = false;
        } else {
            licenseLayout.setError(null);
        }

        if (!valid) {
            android.util.Log.w("RentA_Booking", "Validation failed for fields");
            Toast.makeText(this, "Please correct the errors above", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }
}
