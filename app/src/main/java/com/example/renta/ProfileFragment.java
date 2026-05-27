package com.example.renta;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private ImageView profileImage;
    private ProgressBar uploadProgressBar;
    private TextView profileNameTv;
    private TextView profileEmailTv;
    private TextView initialsTv;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        uploadImageToFirebase(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        profileImage = view.findViewById(R.id.profile_image);
        uploadProgressBar = view.findViewById(R.id.profile_upload_progress);
        profileNameTv = view.findViewById(R.id.profile_name_tv);
        profileEmailTv = view.findViewById(R.id.profile_email_tv);
        initialsTv = view.findViewById(R.id.profile_initials_tv);

        // Header and Menu Items
        View btnNotificationsHeader = view.findViewById(R.id.btn_notifications_header);
        View menuPersonalInfo = view.findViewById(R.id.menu_personal_info);
        View menuPaymentMethods = view.findViewById(R.id.menu_payment_methods);
        View menuDocuments = view.findViewById(R.id.menu_documents);
        View menuNotifications = view.findViewById(R.id.menu_notifications);
        View menuHelp = view.findViewById(R.id.menu_help);
        View menuLogout = view.findViewById(R.id.menu_logout);

        loadUserProfile();

        if (profileImage != null) {
            profileImage.setOnClickListener(v -> openImagePicker());
        }

        if (btnNotificationsHeader != null) {
            btnNotificationsHeader.setOnClickListener(v -> navigateToNotifications());
        }

        if (menuPersonalInfo != null) {
            menuPersonalInfo.setOnClickListener(v -> showEditProfileDialog());
        }

        if (menuPaymentMethods != null) {
            menuPaymentMethods.setOnClickListener(v -> Toast.makeText(getContext(), "Payment Methods coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (menuDocuments != null) {
            menuDocuments.setOnClickListener(v -> Toast.makeText(getContext(), "Documents coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> navigateToNotifications());
        }

        if (menuHelp != null) {
            menuHelp.setOnClickListener(v -> Toast.makeText(getContext(), "Help and Support coming soon!", Toast.LENGTH_SHORT).show());
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> showLogoutConfirmation());
        }

        return view;
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            if (user.isAnonymous()) {
                name = "Guest User";
                email = "Anonymous Account";
            }

            if (profileNameTv != null) profileNameTv.setText(name != null ? name : "User");
            if (profileEmailTv != null) profileEmailTv.setText(email != null ? email : "");
            
            if (initialsTv != null) {
                if (name != null && !name.isEmpty()) {
                    String[] parts = name.split(" ");
                    StringBuilder initials = new StringBuilder();
                    for (int i = 0; i < Math.min(parts.length, 2); i++) {
                        if (!parts[i].isEmpty()) {
                            initials.append(parts[i].charAt(0));
                        }
                    }
                    initialsTv.setText(initials.toString().toUpperCase());
                } else {
                    initialsTv.setText("??");
                }
            }

            if (photoUrl != null && profileImage != null) {
                initialsTv.setVisibility(View.GONE);
                Glide.with(this).load(photoUrl).into(profileImage);
            } else {
                if (initialsTv != null) initialsTv.setVisibility(View.VISIBLE);
                if (profileImage != null) profileImage.setImageDrawable(null);
            }
        }
    }

    private void navigateToNotifications() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new NotificationsFragment())
                    .commit();
            if (getActivity() instanceof HomeActivity) {
                androidx.appcompat.app.ActionBar actionBar = ((HomeActivity) getActivity()).getSupportActionBar();
                if (actionBar != null) actionBar.setTitle("Notifications");
            }
        }
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton("Yes", (dialog, which) -> {
                    android.content.SharedPreferences prefs = requireContext().getSharedPreferences("renta_prefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("auto_login", false).apply();

                    mAuth.signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) getActivity().finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void openImagePicker() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || user.isAnonymous()) {
            Toast.makeText(getContext(), "Please sign up to change profile picture", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void uploadImageToFirebase(Uri imageUri) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        if (uploadProgressBar != null) uploadProgressBar.setVisibility(View.VISIBLE);

        StorageReference profileRef = storage.getReference().child("profile_pictures/" + user.getUid() + ".jpg");

        profileRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(uri)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (uploadProgressBar != null) uploadProgressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    loadUserProfile();
                    Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
                }
            });
        })).addOnFailureListener(e -> {
            if (uploadProgressBar != null) uploadProgressBar.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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

            String newEmail = emailInput.getText().toString();
            if (!newEmail.isEmpty() && !newEmail.equals(user.getEmail())) {
                user.updateEmail(newEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        loadUserProfile();
                        Toast.makeText(getContext(), "Email updated!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Email update failed. You might need to re-login.", Toast.LENGTH_LONG).show();
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
    }
}
