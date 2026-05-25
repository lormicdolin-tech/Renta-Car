package com.example.renta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
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
    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView carName, dates, totalCost, paymentMethod, downPayment;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            carName = itemView.findViewById(R.id.booking_car_name);
            dates = itemView.findViewById(R.id.booking_dates);
            totalCost = itemView.findViewById(R.id.booking_total_cost);
            paymentMethod = itemView.findViewById(R.id.booking_payment_method);
            downPayment = itemView.findViewById(R.id.booking_downpayment);
        }
    }
}
