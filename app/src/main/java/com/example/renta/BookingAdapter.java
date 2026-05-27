package com.example.renta;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * BookingAdapter: A bridge between the list of Bookings and the RecyclerView.
 * Manages display and interactions for booking records, supporting both User and Admin views.
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

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.carName.setText(booking.getCarName());
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String dateRange = sdf.format(new Date(booking.getStartDate())) + " - " + sdf.format(new Date(booking.getEndDate()));
        holder.dates.setText(dateRange);
        
        holder.paymentMethod.setText(booking.getPaymentMethod());
        holder.downPayment.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getDownPayment()));
        holder.totalCost.setText(String.format(Locale.getDefault(), "₱%,.2f", booking.getTotalCost()));
        
        String status = booking.getStatus() != null ? booking.getStatus() : "Pending";
        holder.status.setText(status);
        holder.cancelBtn.setVisibility(View.GONE);

        // Styling and visibility based on status
        switch (status) {
            case "Confirmed":
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.available_green));
                break;
            case "Completed":
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimary));
                // Allow users to remove completed bookings from history
                if (!isAdmin) {
                    holder.cancelBtn.setVisibility(View.VISIBLE);
                    if (holder.cancelBtn instanceof MaterialButton) {
                        ((MaterialButton) holder.cancelBtn).setText(R.string.remove);
                    }
                }
                break;
            case "Cancelled":
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.error_red));
                // Show "Remove" button for already cancelled bookings so users can delete them
                if (!isAdmin) {
                    holder.cancelBtn.setVisibility(View.VISIBLE);
                    if (holder.cancelBtn instanceof MaterialButton) {
                        ((MaterialButton) holder.cancelBtn).setText(R.string.remove);
                    }
                }
                break;
            case "Pending":
            default:
                holder.status.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.text_secondary));
                // Show "Cancel" for pending bookings
                if (!isAdmin) {
                    holder.cancelBtn.setVisibility(View.VISIBLE);
                    if (holder.cancelBtn instanceof MaterialButton) {
                        ((MaterialButton) holder.cancelBtn).setText(R.string.cancel_action);
                    }
                }
                break;
        }

        holder.cancelBtn.setOnClickListener(v -> handleAction(v, booking));

        if (isAdmin) {
            holder.customerInfo.setVisibility(View.VISIBLE);
            String guestLabel = booking.isGuestBooking() ? " [GUEST]" : "";
            String info = (booking.getCustomerName() != null ? booking.getCustomerName() : "Unknown") + guestLabel + "\n" +
                         "Phone: " + booking.getPhoneNumber() + "\n" +
                         "License: " + (booking.getLicenseNumber() != null ? booking.getLicenseNumber() : "N/A") + "\n" +
                         "Address: " + (booking.getAddress() != null ? booking.getAddress() : "N/A");
            holder.customerInfo.setText(info);
            
            holder.deleteBtn.setVisibility(View.VISIBLE);
            holder.deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(v.getContext())
                    .setTitle("Delete Booking")
                    .setMessage("Permanently delete this record from the system?")
                    .setPositiveButton("Delete", (d, w) -> deleteBookingPermanently(booking))
                    .setNegativeButton("Cancel", null)
                    .show();
            });

            holder.itemView.setOnClickListener(v -> showStatusUpdateDialog(v.getContext(), booking));
        } else {
            holder.customerInfo.setVisibility(View.GONE);
            holder.deleteBtn.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), BookingDetailViewActivity.class);
                intent.putExtra("booking", booking);
                v.getContext().startActivity(intent);
            });
        }
    }

    private void handleAction(View v, Booking booking) {
        String currentStatus = booking.getStatus() != null ? booking.getStatus() : "Pending";
        boolean isRemoval = "Cancelled".equals(currentStatus) || "Completed".equals(currentStatus);
        String actionTitle = isRemoval ? v.getContext().getString(R.string.remove) : v.getContext().getString(R.string.cancel_action);
        String message = isRemoval ? "Remove this record from your history?" : "Are you sure you want to cancel this booking?";

        new AlertDialog.Builder(v.getContext())
            .setTitle(actionTitle + " Booking")
            .setMessage(message)
            .setPositiveButton("Yes", (dialog, which) -> {
                DatabaseReference ref = FirebaseConfig.getDatabase().getReference("bookings").child(booking.getBookingId());
                if (isRemoval) {
                    ref.removeValue().addOnSuccessListener(aVoid -> 
                        Toast.makeText(v.getContext(), "Record removed", Toast.LENGTH_SHORT).show());
                } else {
                    ref.child("status").setValue("Cancelled").addOnSuccessListener(aVoid -> {
                        Toast.makeText(v.getContext(), "Booking cancelled", Toast.LENGTH_SHORT).show();
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            saveNotification(user.getUid(), "Booking Cancelled", 
                                "You have cancelled your booking for " + booking.getCarName());
                        }
                    });
                }
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void deleteBookingPermanently(Booking booking) {
        if (booking.getBookingId() != null) {
            FirebaseConfig.getDatabase().getReference("bookings").child(booking.getBookingId()).removeValue();
        }
    }

    private void showStatusUpdateDialog(android.content.Context context, Booking booking) {
        String[] statuses = {"Pending", "Confirmed", "Completed", "Cancelled"};
        int checkedItem = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(booking.getStatus())) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(context)
            .setTitle("Update Booking Status")
            .setSingleChoiceItems(statuses, checkedItem, (dialog, which) -> {
                String newStatus = statuses[which];
                if (booking.getBookingId() != null) {
                    FirebaseConfig.getDatabase().getReference("bookings")
                        .child(booking.getBookingId()).child("status").setValue(newStatus)
                        .addOnSuccessListener(aVoid -> {
                            if (booking.getUserId() != null) {
                                String title = "Booking " + newStatus;
                                String message = "Your booking for " + booking.getCarName() + " is now " + newStatus.toLowerCase();
                                saveNotification(booking.getUserId(), title, message);
                            }
                        });
                }
                dialog.dismiss();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void saveNotification(String userId, String title, String message) {
        DatabaseReference notifRef = FirebaseConfig.getDatabase().getReference("notifications").child(userId);
        String id = notifRef.push().getKey();
        if (id != null) {
            Notification n = new Notification(id, userId, title, message, System.currentTimeMillis());
            notifRef.child(id).setValue(n);
        }
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView carName, dates, totalCost, paymentMethod, downPayment, status, customerInfo;
        View deleteBtn, cancelBtn;

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
            cancelBtn = itemView.findViewById(R.id.booking_cancel_btn);
        }
    }
}
