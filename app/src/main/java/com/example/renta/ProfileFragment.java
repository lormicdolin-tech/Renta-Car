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
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;
// import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;

public class ProfileFragment extends Fragment {

    private TextView profileNameTv;
    private TextView profileEmailTv;
    private TextView rentalsCountTv;
    private UserManager userManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileNameTv = view.findViewById(R.id.profile_name_tv);
        profileEmailTv = view.findViewById(R.id.profile_email_tv);
        rentalsCountTv = view.findViewById(R.id.stats_rentals_count);
        MaterialButton editProfileBtn = view.findViewById(R.id.edit_profile_btn);

        userManager = new UserManager(requireContext());
        loadUserProfile();
        updateBookingStats();

        if (editProfileBtn != null) {
            editProfileBtn.setOnClickListener(v -> showEditProfileDialog());
        }

        return view;
    }

    private void loadUserProfile() {
        User user = userManager.getUser();
        if (profileNameTv != null) profileNameTv.setText(user.getName());
        if (profileEmailTv != null) profileEmailTv.setText(user.getEmail());
    }

    private void updateBookingStats() {
        BookingManager bookingManager = new BookingManager(requireContext());
        List<Booking> bookings = bookingManager.getBookings();
        if (rentalsCountTv != null) {
            rentalsCountTv.setText(String.valueOf(bookings.size()));
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_profile_title);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_name_input);
        EditText emailInput = dialogView.findViewById(R.id.edit_email_input);
        EditText passwordInput = dialogView.findViewById(R.id.edit_password_input);

        User currentUser = userManager.getUser();
        nameInput.setText(currentUser.getName());
        emailInput.setText(currentUser.getEmail());
        passwordInput.setText(currentUser.getPassword());

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString();
            String newEmail = emailInput.getText().toString();
            String newPassword = passwordInput.getText().toString();
            if (!newName.isEmpty() && !newEmail.isEmpty()) {
                User updatedUser = new User(newName, newEmail, newPassword);
                userManager.saveUser(updatedUser);
                loadUserProfile();
                Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
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
