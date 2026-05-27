package com.example.renta;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * AdminCarAdapter: An adapter for the RecyclerView in AdminActivity.
 * Displays all cars and allows administrators to toggle their availability status
 * which is then synchronized with the Firebase Realtime Database.
 */
public class AdminCarAdapter extends RecyclerView.Adapter<AdminCarAdapter.CarViewHolder> {

    private final List<Car> carList;
    private final DatabaseReference mDatabase;

    /**
     * Constructor for AdminCarAdapter.
     * @param carList The list of Car objects to be displayed.
     */
    public AdminCarAdapter(List<Car> carList) {
        this.carList = carList;
        // Reference to the 'cars' node in the database
        this.mDatabase = FirebaseConfig.getDatabase().getReference().child("cars");
    }

    @NonNull
    @Override
    public CarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual admin car items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_car, parent, false);
        return new CarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarViewHolder holder, int position) {
        Car car = carList.get(position);
        
        // Populate UI with car details
        holder.nameTv.setText(car.getName());
        holder.priceTv.setText(car.getPrice());
        
        // Set the availability switch state based on the car's current status
        holder.availabilitySwitch.setChecked(car.isAvailable());
        
        // Set the car image if a resource ID is provided
        if (car.getImageResId() != 0) {
            holder.imageIv.setImageResource(car.getImageResId());
        }

        /**
         * Listener for the availability switch.
         * When the switch is toggled, it updates the local car object and
         * persists the new availability status to the Firebase database.
         */
        holder.availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            car.setAvailable(isChecked);
            if (car.getId() != null) {
                // Update 'available' property in Firebase
                mDatabase.child(car.getId()).child("available").setValue(isChecked);
            }
        });

        holder.editBtn.setOnClickListener(v -> {
            showEditCarDialog(v.getContext(), car);
        });
    }

    private void showEditCarDialog(android.content.Context context, Car car) {
        // Implementation for editing car details
        android.widget.EditText editText = new android.widget.EditText(context);
        editText.setText(car.getPrice());
        
        new androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Edit Price for " + car.getName())
            .setView(editText)
            .setPositiveButton("Update", (dialog, which) -> {
                String newPrice = editText.getText().toString();
                if (car.getId() != null) {
                    mDatabase.child(car.getId()).child("price").setValue(newPrice);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public int getItemCount() {
        return carList.size();
    }

    /**
     * CarViewHolder: View holder class for the AdminCarAdapter.
     * Holds references to the UI components for efficient recycling.
     */
    static class CarViewHolder extends RecyclerView.ViewHolder {
        ImageView imageIv, editBtn;
        TextView nameTv, priceTv;
        SwitchMaterial availabilitySwitch;

        public CarViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI components from the layout
            imageIv = itemView.findViewById(R.id.admin_car_image);
            nameTv = itemView.findViewById(R.id.admin_car_name);
            priceTv = itemView.findViewById(R.id.admin_car_price);
            availabilitySwitch = itemView.findViewById(R.id.admin_car_availability_switch);
            editBtn = itemView.findViewById(R.id.admin_car_edit_btn);
        }
    }
}
