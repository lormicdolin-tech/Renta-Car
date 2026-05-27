package com.example.renta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BookingAdapter: A bridge between the list of Bookings and the RecyclerView in BookingsFragment.
 * It manages how each individual booking item is displayed in the list.
 */
public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private final List<Booking> bookings;
    private boolean isAdmin = false;

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public BookingAdapter(List<Booking> bookings, boolean isAdmin) {
        this.bookings = bookings;
        this.isAdmin = isAdmin;
    }

    /**
     * Inflates the item layout (item_booking.xml) when a new row is needed.
     */
    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    /**
     * Binds data from a Booking object to the UI elements in a specific row.
     */
    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.carName.setText(booking.getCarName());
        
        // Format dates from milliseconds to "MMM dd, yyyy"
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateRange = sdf.format(new Date(booking.getStartDate())) + " - " + sdf.format(new Date(booking.getEndDate()));
        holder.dates.setText(dateRange);
        
        holder.paymentMethod.setText(booking.getPaymentMethod());
        
        // Format currency values with comma separators and peso sign
        holder.downPayment.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getDownPayment()));
        holder.totalCost.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getTotalCost()));
        
        if (booking.getStatus() != null) {
            holder.status.setText(booking.getStatus());
            if (booking.getStatus().equals("Confirmed")) {
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.available_green));
            } else if (booking.getStatus().equals("Cancelled")) {
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
            } else {
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
            }
        }

        if (isAdmin) {
            holder.customerInfo.setVisibility(View.VISIBLE);
            String guestLabel = booking.isGuestBooking() ? " [GUEST]" : "";
            
            StringBuilder info = new StringBuilder();
            info.append(booking.getCustomerName() != null ? booking.getCustomerName() : "Unknown").append(guestLabel).append("\n");
            info.append("Phone: ").append(booking.getPhoneNumber()).append("\n");
            info.append("License: ").append(booking.getLicenseNumber() != null ? booking.getLicenseNumber() : "N/A").append("\n");
            info.append("Address: ").append(booking.getAddress() != null ? booking.getAddress() : "N/A");
            
            holder.customerInfo.setText(info.toString());
            
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(v -> {
                if (booking.getBookingId() != null) {
                    FirebaseDatabase.getInstance().getReference().child("bookings")
                        .child(booking.getBookingId()).removeValue();
                }
            });

            holder.itemView.setOnClickListener(v -> {
                // Potential to add status update dialog here
                showStatusUpdateDialog(v.getContext(), booking);
            });
        } else {
            holder.customerInfo.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(null);
        }
    }

    private void showStatusUpdateDialog(android.content.Context context, Booking booking) {
        String[] statuses = {"Pending", "Confirmed", "Cancelled"};
        int checkedItem = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(booking.getStatus())) {
                checkedItem = i;
                break;
            }
        }

        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Update Booking Status")
            .setSingleChoiceItems(statuses, checkedItem, (dialog, which) -> {
                String newStatus = statuses[which];
                if (booking.getBookingId() != null) {
                    FirebaseDatabase.getInstance().getReference().child("bookings")
                        .child(booking.getBookingId()).child("status").setValue(newStatus);
                }
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Returns the total number of bookings in the list.
     */
    @Override
    public int getItemCount() {
        return bookings.size();
    }

    /**
     * BookingViewHolder: Holds references to the views for a single booking item.
     * This improves performance by avoiding frequent calls to findViewById.
     */
    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView carName, dates, totalCost, paymentMethod, downPayment, status, customerInfo;
        View deleteBtn;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.booking_car_name);
            dates = itemView.findViewById(R.id.booking_dates);
            totalCost = itemView.findViewById(R.id.booking_total_cost);
            paymentMethod = itemView.findViewById(R.id.booking_payment_method);
            downPayment = itemView.findViewById(R.id.booking_downpayment);
            status = itemView.findViewById(R.id.booking_status);
            deleteBtn = itemView.findViewById(R.id.booking_delete_btn);
            customerInfo = itemView.findViewById(R.id.booking_customer_info);
        }
    }
}
