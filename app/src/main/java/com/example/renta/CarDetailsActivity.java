package com.example.renta;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import android.content.Intent;
import androidx.core.content.ContextCompat;

/**
 * CarDetailsActivity: Displays detailed information about a selected car and 
 * provides a booking system with date picking, tiered cost calculation, and notifications.
 */
public class CarDetailsActivity extends AppCompatActivity {

    private Long startDate;      // Rental start date in milliseconds
    private Long endDate;        // Rental end date in milliseconds
    private MaterialButton startDateBtn;
    private MaterialButton endDateBtn;
    private TextView totalCostTv;
    private String carName;
    private String carPriceStr;
    private double dailyRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);
        
        // Retrieve car data passed from the home screen
        carName = getIntent().getStringExtra("car_name");
        carPriceStr = getIntent().getStringExtra("car_price");
        if (carName == null) carName = "Toyota Vios";
        if (carPriceStr == null) carPriceStr = "₱2,000/day";
        
        // Parse daily rate for calculations (e.g., "₱2,000/day" -> 2000.0)
        try {
            String cleanPrice = carPriceStr.replace("₱", "").replace(",", "").split("/")[0].trim();
            dailyRate = Double.parseDouble(cleanPrice);
        } catch (Exception e) {
            dailyRate = 2000.0; // Fallback default rate
        }

        // Link UI components
        TextView carNameTv = findViewById(R.id.car_name_detail);
        TextView carPriceTv = findViewById(R.id.car_price_detail);
        TextView carDescTv = findViewById(R.id.car_description);
        android.widget.ImageView carIv = findViewById(R.id.car_image);
        TextView fuelTv = findViewById(R.id.spec_fuel_value);
        TextView seatsTv = findViewById(R.id.spec_seats_value);
        TextView transTv = findViewById(R.id.spec_trans_value);
        TextView condTv = findViewById(R.id.spec_cond_value);

        // Bind data to views
        carNameTv.setText(carName);
        carPriceTv.setText(carPriceStr);

        int imageResId = getIntent().getIntExtra("car_image", R.drawable.vios);
        carIv.setImageResource(imageResId);
        
        // Set specific car details
        String desc = getIntent().getStringExtra("car_desc");
        if (desc != null) carDescTv.setText(desc);
        
        String fuel = getIntent().getStringExtra("car_fuel");
        if (fuel != null) fuelTv.setText(fuel);
        
        String seats = getIntent().getStringExtra("car_seats");
        if (seats != null) seatsTv.setText(seats);
        
        String trans = getIntent().getStringExtra("car_trans");
        if (trans != null) transTv.setText(trans);

        String cond = getIntent().getStringExtra("car_cond");
        if (cond != null) condTv.setText(cond);

        // Configure toolbar with back navigation
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup booking UI controls
        startDateBtn = findViewById(R.id.start_date_btn);
        endDateBtn = findViewById(R.id.end_date_btn);
        totalCostTv = findViewById(R.id.total_cost_tv);
        MaterialButton bookNowBtn = findViewById(R.id.book_now_btn);

        startDateBtn.setOnClickListener(v -> showDatePicker(true));
        endDateBtn.setOnClickListener(v -> showDatePicker(false));

        bookNowBtn.setOnClickListener(v -> {
            // Validation: Ensure both dates are picked
            if (startDate == null || endDate == null) {
                Toast.makeText(this, R.string.please_select_dates, Toast.LENGTH_SHORT).show();
                return;
            }
            showBookingInfoDialog();
        });
    }

    /**
     * showBookingInfoDialog: Handles user input for phone, payment, and calculates tiered pricing.
     * Tiered Pricing: Registered users get 10% off; Guests pay full price.
     * Downpayment: 20% of the final total.
     */
    private void showBookingInfoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_info, null);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phone_edit_text);
        RadioGroup paymentGroup = dialogView.findViewById(R.id.payment_radio_group);
        TextView downpaymentTv = dialogView.findViewById(R.id.downpayment_summary);

        // Determine user type (Registered vs Guest)
        com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        boolean isGuest = user == null || user.isAnonymous() || "guest@renta.com".equals(user.getEmail());

        // Cost and Discount Calculation
        double totalCost = calculateTotalCost();
        double discount = 0;
        
        if (!isGuest) {
            // Apply 10% promo for registered users
            discount = totalCost * 0.10;
            totalCost -= discount;
        }

        double downPayment = totalCost * 0.20;
        String downPaymentFormatted = String.format(Locale.getDefault(), "₱%,.2f", downPayment);
        
        // Build summary text for the dialog
        StringBuilder summary = new StringBuilder();
        if (!isGuest) {
            summary.append("Registered User Discount (10%): -₱").append(String.format(Locale.getDefault(), "%,.2f", discount)).append("\n");
        } else {
            summary.append("Guest User: No discounts available. Sign up to get 10% off!\n");
        }
        summary.append(getString(R.string.downpayment_label, downPaymentFormatted));
        
        downpaymentTv.setText(summary.toString());

        final double finalTotalCost = totalCost;
        final double finalDownPayment = downPayment;

        // Create the confirmation dialog
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_booking)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm_booking, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            
            // Stylize buttons
            positiveButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            negativeButton.setTextColor(ContextCompat.getColor(this, android.R.color.black));

            positiveButton.setOnClickListener(view -> {
                String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";
                TextInputLayout phoneLayout = dialogView.findViewById(R.id.phone_input_layout);

                // Validation: Check phone format
                if (phone.isEmpty()) {
                    phoneLayout.setError(getString(R.string.error_empty_phone));
                    return;
                }
                if (!phone.matches("^(09|\\+639)\\d{9}$")) {
                    phoneLayout.setError(getString(R.string.error_invalid_phone));
                    return;
                }

                phoneLayout.setError(null);

                // Identify selected payment method
                int selectedId = paymentGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(CarDetailsActivity.this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selectedRb = dialogView.findViewById(selectedId);
                String paymentMethod = selectedRb.getText().toString();

                // Create Booking object and pass it to information collection screen
                Booking booking = new Booking(carName, carPriceStr, startDate, endDate, finalTotalCost,
                        finalDownPayment, paymentMethod, phone);
                booking.setGuestBooking(isGuest);
                
                Intent intent = new Intent(CarDetailsActivity.this, BookingDetailsActivity.class);
                intent.putExtra("pending_booking", booking);
                startActivity(intent);

                dialog.dismiss();
            });
        });

        dialog.show();
    }

    /**
     * showDatePicker: Displays MaterialDatePicker and updates selected timestamps.
     */
    private void showDatePicker(boolean isStartDate) {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(isStartDate ? R.string.start_date : R.string.end_date)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String dateString = sdf.format(new Date(selection));

            if (isStartDate) {
                startDate = selection;
                startDateBtn.setText(dateString);
            } else {
                endDate = selection;
                endDateBtn.setText(dateString);
            }

            // Dynamically update cost label based on date range
            updateTotalCost();
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Updates the total cost display in the UI.
     */
    private void updateTotalCost() {
        if (startDate != null && endDate != null) {
            double total = calculateTotalCost();
            String totalFormatted = String.format(Locale.getDefault(), "₱%,.2f", total);
            totalCostTv.setText(getString(R.string.total_cost, totalFormatted));
            totalCostTv.setVisibility(View.VISIBLE);
        }
    }

    /**
     * calculateTotalCost: Calculates the raw rental cost based on day count.
     */
    private double calculateTotalCost() {
        if (startDate != null && endDate != null) {
            long diff = endDate - startDate;
            long days = diff / (24 * 60 * 60 * 1000);
            if (days < 1) days = 1; // Minimum charge is 1 day
            return days * dailyRate;
        }
        return 0;
    }
}
