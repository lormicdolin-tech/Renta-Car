package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

/**
 * HomeFragment: The landing screen within HomeActivity.
 * Displays a list of available cars and handles clicks to view their details.
 */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // List of button IDs for car detail buttons
        int[] buttonIds = {
            R.id.car_1_details_btn,
            R.id.car_2_details_btn,
            R.id.car_3_details_btn,
            R.id.car_4_details_btn,
            R.id.car_5_details_btn
        };

        // Common click listener to handle car selection
        View.OnClickListener detailsClickListener = v -> {
            Intent intent = new Intent(getActivity(), CarDetailsActivity.class);
            
            // Pass specific car data based on which button was clicked
            int id = v.getId();
            if (id == R.id.car_1_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_1_name));
                intent.putExtra("car_price", getString(R.string.car_1_price));
                intent.putExtra("car_desc", getString(R.string.car_1_desc));
                intent.putExtra("car_image", R.drawable.vios);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_2_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_2_name));
                intent.putExtra("car_price", getString(R.string.car_2_price));
                intent.putExtra("car_desc", getString(R.string.car_2_desc));
                intent.putExtra("car_image", R.drawable.mirage);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Manual/CVT");
                intent.putExtra("car_cond", "Excellent");
            } else if (id == R.id.car_3_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_3_name));
                intent.putExtra("car_price", getString(R.string.car_3_price));
                intent.putExtra("car_desc", getString(R.string.car_3_desc));
                intent.putExtra("car_image", R.drawable.innova);
                intent.putExtra("car_fuel", "Diesel");
                intent.putExtra("car_seats", "7 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_4_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_4_name));
                intent.putExtra("car_price", getString(R.string.car_4_price));
                intent.putExtra("car_desc", getString(R.string.car_4_desc));
                intent.putExtra("car_image", R.drawable.fortuner);
                intent.putExtra("car_fuel", "Diesel");
                intent.putExtra("car_seats", "7 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_5_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_5_name));
                intent.putExtra("car_price", getString(R.string.car_5_price));
                intent.putExtra("car_desc", getString(R.string.car_5_desc));
                intent.putExtra("car_image", R.drawable.wigo);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Manual");
                intent.putExtra("car_cond", "Excellent");
            }

            // Start the details activity
            startActivity(intent);
        };

        // Assign the listener to all car detail buttons
        for (int id : buttonIds) {
            MaterialButton btn = view.findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(detailsClickListener);
            }
        }

        return view;
    }
}
