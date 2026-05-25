package com.example.renta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.button.MaterialButton;

public class LocationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        MaterialButton directionsBtn = view.findViewById(R.id.get_directions_btn);
        directionsBtn.setOnClickListener(v -> {
            // Coordinate for UM Digos College
            String uri = "geo:6.7486,125.3565?q=6.7486,125.3565(RentA+Main+Hub)";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                // Fallback to any app that can handle geo URIs
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(uri)));
                } catch (Exception e) {
                    android.widget.Toast.makeText(getContext(), "Maps app not found", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }
}
