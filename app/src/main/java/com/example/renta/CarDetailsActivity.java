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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * CarDetailsActivity: Displays detailed information about a selected car and 
 * provides a booking system with date picking, cost calculation, and notifications.
 */
public class CarDetailsActivity extends AppCompatActivity {

    private Long startDate;      // Rental start date in ms
    private Long endDate;        // Rental end date in ms
    private MaterialButton startDateBtn;
    private MaterialButton endDateBtn;
    private TextView totalCostTv;
    private String carName;
    private String carPriceStr;
    private double dailyRate;

    private String pendingNotifTitle;
    private String pendingNotifMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);
        
        createNotificationChannel();

        // Retrieve car data passed from HomeFragment via Intent
        carName = getIntent().getStringExtra("car_name");
        carPriceStr = getIntent().getStringExtra("car_price");
        if (carName == null) carName = "Toyota Vios";
        if (carPriceStr == null) carPriceStr = "₱2,000/day";
        
        // Parse the daily rate string to a double for calculations
        try {
            // Remove ₱, commas, and "/day" to get the numeric value
            String cleanPrice = carPriceStr.replace("₱", "").replace(",", "").split("/")[0].trim();
            dailyRate = Double.parseDouble(cleanPrice);
        } catch (Exception e) {
            dailyRate = 2000.0; // Fallback rate
        }

        // Initialize UI components
        TextView carNameTv = findViewById(R.id.car_name_detail);
        TextView carPriceTv = findViewById(R.id.car_price_detail);
        TextView carDescTv = findViewById(R.id.car_description);
        android.widget.ImageView carIv = findViewById(R.id.car_image);
        TextView fuelTv = findViewById(R.id.spec_fuel_value);
        TextView seatsTv = findViewById(R.id.spec_seats_value);
        TextView transTv = findViewById(R.id.spec_trans_value);
        TextView condTv = findViewById(R.id.spec_cond_value);

        // Display the retrieved car data
        carNameTv.setText(carName);
        carPriceTv.setText(carPriceStr);

        int imageResId = getIntent().getIntExtra("car_image", R.drawable.vios);
        carIv.setImageResource(imageResId);
        
        // Set dynamic specifications
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

        // Set up the top toolbar with a back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Booking system initialization
        startDateBtn = findViewById(R.id.start_date_btn);
        endDateBtn = findViewById(R.id.end_date_btn);
        totalCostTv = findViewById(R.id.total_cost_tv);
        MaterialButton bookNowBtn = findViewById(R.id.book_now_btn);

        startDateBtn.setOnClickListener(v -> showDatePicker(true));
        endDateBtn.setOnClickListener(v -> showDatePicker(false));

        bookNowBtn.setOnClickListener(v -> {
            // Ensure both dates are selected before proceeding
            if (startDate == null || endDate == null) {
                Toast.makeText(this, R.string.please_select_dates, Toast.LENGTH_SHORT).show();
                return;
            }
            showBookingInfoDialog();
        });
    }

    /**
     * showBookingInfoDialog: Shows a pop-up to collect phone number and payment method.
     * Calculates the downpayment (20%) and saves the booking upon confirmation.
     */
    private void showBookingInfoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_booking_info, null);
        TextInputEditText phoneInput = dialogView.findViewById(R.id.phone_edit_text);
        RadioGroup paymentGroup = dialogView.findViewById(R.id.payment_radio_group);
        TextView downpaymentTv = dialogView.findViewById(R.id.downpayment_summary);

        // Calculate total and 20% downpayment
        double totalCost = calculateTotalCost();
        double downPayment = totalCost * 0.20;
        String downPaymentFormatted = String.format(Locale.getDefault(), "₱%,.2f", downPayment);
        downpaymentTv.setText(getString(R.string.downpayment_label, downPaymentFormatted));

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirm_booking)
                .setView(dialogView)
                .setPositiveButton(R.string.confirm_booking, null) // Set to null to override behavior below
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            
            // Stylize buttons for M3
            positiveButton.setTextColor(getResources().getColor(android.R.color.black));
            negativeButton.setTextColor(getResources().getColor(android.R.color.black));

            positiveButton.setOnClickListener(view -> {
                String phone = phoneInput.getText().toString().trim();
                TextInputLayout phoneLayout = dialogView.findViewById(R.id.phone_input_layout);

                // Phone number validation (Philippine format)
                if (phone.isEmpty()) {
                    phoneLayout.setError(getString(R.string.error_empty_phone));
                    return;
                }

                if (!phone.matches("^(09|\\+639)\\d{9}$")) {
                    phoneLayout.setError(getString(R.string.error_invalid_phone));
                    return;
                }

                phoneLayout.setError(null);

                // Get selected payment method
                int selectedId = paymentGroup.getCheckedRadioButtonId();
                RadioButton selectedRb = dialogView.findViewById(selectedId);
                String paymentMethod = selectedRb.getText().toString();

                // Create and save the booking
                Booking booking = new Booking(carName, carPriceStr, startDate, endDate, totalCost,
                        downPayment, paymentMethod, phone);
                new BookingManager(this).addBooking(booking);

                // Trigger a system notification for the user
                boolean permissionRequested = showNotification("Booking Confirmed", "Your rental for " + carName + " has been successfully processed.");

                Toast.makeText(this, R.string.booking_confirmed, Toast.LENGTH_LONG).show();
                dialog.dismiss();
                if (!permissionRequested) {
                    finish(); // Close activity and return to Home
                }
            });
        });

        dialog.show();
    }

    /**
     * showDatePicker: Opens the Material Design date picker.
     * @param isStartDate True if picking the start date, false for end date.
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

            updateTotalCost(); // Update the cost display dynamically
        });

        datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
    }

    /**
     * Updates the UI with the calculated total rental cost.
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
     * showNotification: Displays a system notification for booking confirmation.
     * Handles notification channel creation and permission checks for Android 13+.
     * @return true if a permission request was initiated, false otherwise.
     */
    private boolean showNotification(String title, String message) {
        // Permission check for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                pendingNotifTitle = title;
                pendingNotifMessage = message;
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
                return true;
            }
        }

        sendActualNotification(title, message);
        return false;
    }

    private void sendActualNotification(String title, String message) {
        String channelId = "booking_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // System icon is safer for SmallIcon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            // Should not happen if permission is granted
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "booking_channel",
                    "Booking Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingNotifTitle != null) {
                    sendActualNotification(pendingNotifTitle, pendingNotifMessage);
                }
            }
            finish(); // Now we can finish the activity
        }
    }

    /**
     * Calculates the total cost based on the number of days between start and end date.
     * Minimum 1 day charge is applied.
     */
    private double calculateTotalCost() {
        if (startDate != null && endDate != null) {
            long diff = endDate - startDate;
            long days = diff / (24 * 60 * 60 * 1000); // Convert ms to days
            if (days < 1) days = 1; // Minimum 1 day charge
            return days * dailyRate;
        }
        return 0;
    }
}
