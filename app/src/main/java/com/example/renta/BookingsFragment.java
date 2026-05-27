package com.example.renta;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class BookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private View emptyState;
    private TabLayout tabLayout;
    private List<Booking> allBookings = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);

        recyclerView = view.findViewById(R.id.bookings_rv);
        emptyState = view.findViewById(R.id.empty_state);
        tabLayout = view.findViewById(R.id.booking_tabs);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterBookings(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadBookings();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        if (getContext() == null) return;
        
        new BookingManager(getContext()).getBookings(bookings -> {
            if (!isAdded()) return;
            
            this.allBookings = bookings;
            filterBookings(tabLayout.getSelectedTabPosition());
        });
    }

    private void filterBookings(int tabPosition) {
        List<Booking> filteredList = new ArrayList<>();
        
        for (Booking booking : allBookings) {
            String status = booking.getStatus();
            if (status == null) status = "Pending";

            switch (tabPosition) {
                case 0: // Upcoming (Pending & Confirmed)
                    if (status.equalsIgnoreCase("Pending") || status.equalsIgnoreCase("Confirmed")) {
                        filteredList.add(booking);
                    }
                    break;
                case 1: // Completed
                    if (status.equalsIgnoreCase("Completed")) {
                        filteredList.add(booking);
                    }
                    break;
                case 2: // Cancelled
                    if (status.equalsIgnoreCase("Cancelled")) {
                        filteredList.add(booking);
                    }
                    break;
            }
        }

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            recyclerView.setAdapter(new BookingAdapter(filteredList));
        }
    }
}
