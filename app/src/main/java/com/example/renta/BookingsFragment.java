package com.example.renta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * BookingsFragment: Displays the user's rental history.
 * It uses a RecyclerView to show a list of bookings and an empty state 
 * view when no bookings are found.
 */
public class BookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        recyclerView = view.findViewById(R.id.bookings_rv);
        emptyState = view.findViewById(R.id.empty_state);

        // Initial load of bookings
        loadBookings();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list whenever the fragment becomes visible again
        loadBookings();
    }

    /**
     * loadBookings: Fetches the list of bookings from BookingManager.
     * Toggles between the list view and empty state view based on results.
     */
    private void loadBookings() {
        if (getContext() == null) return;
        
        new BookingManager(getContext()).getBookings(bookings -> {
            if (!isAdded()) return; // Check if fragment is still attached
            
            if (bookings.isEmpty()) {
                // Show "No bookings" message
                recyclerView.setVisibility(View.GONE);
                emptyState.setVisibility(View.VISIBLE);
            } else {
                // Show the list of bookings
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);
                recyclerView.setAdapter(new BookingAdapter(bookings));
            }
        });
    }
}
