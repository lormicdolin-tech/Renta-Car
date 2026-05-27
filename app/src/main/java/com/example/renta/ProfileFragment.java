package com.example.renta;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView profileNameTv;
    private TextView profileEmailTv;
    private TextView rentalsCountTv;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        profileNameTv = view.findViewById(R.id.profile_name_tv);
        profileEmailTv = view.findViewById(R.id.profile_email_tv);
        rentalsCountTv = view.findViewById(R.id.stats_rentals_count);
        MaterialButton editProfileBtn = view.findViewById(R.id.edit_profile_btn);

        loadUserProfile();
        updateBookingStats();

        if (editProfileBtn != null) {
            editProfileBtn.setOnClickListener(v -> showEditProfileDialog());
        }

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (user.isAnonymous()) {
                name = "Guest User";
                email = "Anonymous Account";
            }

            if (profileNameTv != null) profileNameTv.setText(name != null ? name : "User");
            if (profileEmailTv != null) profileEmailTv.setText(email != null ? email : "");
        }
    }

    private void updateBookingStats() {
        if (getContext() == null) return;
        BookingManager bookingManager = new BookingManager(requireContext());
        bookingManager.getBookings(bookings -> {
            if (isAdded() && rentalsCountTv != null) {
                rentalsCountTv.setText(String.valueOf(bookings.size()));
            }
        });
    }

    private void showEditProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;
        
        if (user.isAnonymous()) {
            Toast.makeText(getContext(), "Please sign up to edit your profile", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_profile_title);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_name_input);
        EditText emailInput = dialogView.findViewById(R.id.edit_email_input);
        EditText passwordInput = dialogView.findViewById(R.id.edit_password_input);

        nameInput.setText(user.getDisplayName());
        emailInput.setText(user.getEmail());
        emailInput.setEnabled(false); // Email usually not editable directly without re-auth
        passwordInput.setHint("Enter new password (optional)");

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString();
            String newPassword = passwordInput.getText().toString();

            if (!newName.isEmpty()) {
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build();

                user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadUserProfile();
                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (!newPassword.isEmpty() && newPassword.length() >= 6) {
                user.updatePassword(newPassword).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Password updated!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateBookingStats();
    }
}
