package com.example.renta;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView bookingsRv, carsRv;
    private ProgressBar progressBar;
    private BookingAdapter bookingAdapter;
    private AdminCarAdapter carAdapter;
    private List<Booking> allBookings = new ArrayList<>();
    private List<Car> allCars = new ArrayList<>();
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        bookingsRv = findViewById(R.id.admin_bookings_rv);
        carsRv = findViewById(R.id.admin_cars_rv);
        progressBar = findViewById(R.id.admin_progress);
        TabLayout tabLayout = findViewById(R.id.admin_tabs);

        bookingsRv.setLayoutManager(new LinearLayoutManager(this));
        carsRv.setLayoutManager(new LinearLayoutManager(this));

        bookingAdapter = new BookingAdapter(allBookings, true);
        bookingsRv.setAdapter(bookingAdapter);

        carAdapter = new AdminCarAdapter(allCars);
        carsRv.setAdapter(carAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    bookingsRv.setVisibility(View.VISIBLE);
                    carsRv.setVisibility(View.GONE);
                } else {
                    bookingsRv.setVisibility(View.GONE);
                    carsRv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadAllData();
    }

    private void loadAllData() {
        progressBar.setVisibility(View.VISIBLE);

        mDatabase.child("bookings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allBookings.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Booking b = ds.getValue(Booking.class);
                    if (b != null) allBookings.add(b);
                }
                bookingAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
            }
        });

        mDatabase.child("cars").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allCars.clear();
                if (!snapshot.exists()) {
                    // Seed initial data if Firebase is empty
                    seedCars();
                } else {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Car c = ds.getValue(Car.class);
                        if (c != null) allCars.add(c);
                    }
                    carAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void seedCars() {
        String[] names = {"Toyota Vios", "Mitsubishi Mirage G4", "Toyota Innova", "Toyota Fortuner", "Toyota Wigo", "Nissan NV350 Urvan", "Toyota Hiace Grandia"};
        String[] prices = {"₱2,000/day", "₱1,800/day", "₱3,500/day", "₱5,000/day", "₱1,500/day", "₱4,500/day", "₱6,000/day"};
        int[] images = {R.drawable.vios, R.drawable.mirage, R.drawable.innova, R.drawable.fortuner, R.drawable.wigo, R.drawable.urvan, R.drawable.hiace};

        for (int i = 0; i < names.length; i++) {
            String id = "car_" + (i + 1);
            Car car = new Car(id, names[i], prices[i], "Description for " + names[i], "Gas/Diesel", "5-15 Persons", "Auto/Manual", "Pristine", images[i], true);
            mDatabase.child("cars").child(id).setValue(car);
        }
    }
}
