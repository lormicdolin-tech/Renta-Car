package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * SignupActivity: Handles new user registration using Firebase Authentication.
 * It collects user details, creates a Firebase account, and sets the display name.
 */
public class SignupActivity extends AppCompatActivity {

    private TextInputLayout fullNameInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private TextInputEditText fullNameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private MaterialButton signupBtn;
    private TextView loginBtn;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth and Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // UI Component Initialization
        fullNameInputLayout = findViewById(R.id.fullNameInputLayout);
        emailInputLayout = findViewById(R.id.emailInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        
        fullNameEdit = findViewById(R.id.fullNameEdit);
        emailEdit = findViewById(R.id.emailEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        confirmPasswordEdit = findViewById(R.id.confirmPasswordEdit);
        
        signupBtn = findViewById(R.id.signupBtn);
        loginBtn = findViewById(R.id.loginBtn);

        // Adjust layout for system bars (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Signup Button Click Listener
        signupBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                String fullName = fullNameEdit.getText().toString().trim();
                String email = emailEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();

                // Create user with Firebase Auth
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Update profile with full name
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(fullName)
                                    .build();

                                user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(updateTask -> {
                                        // Save user details to Database
                                        User newUser = new User(fullName, email);
                                        mDatabase.child("users").child(user.getUid()).setValue(newUser)
                                            .addOnCompleteListener(dbTask -> {
                                                Toast.makeText(SignupActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                                    });
                            }
                        } else {
                            Toast.makeText(SignupActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
            }
        });

        // Go back to Login screen
        loginBtn.setOnClickListener(v -> finish());
    }

    /**
     * Validates user inputs for registration.
     * Checks for empty fields, valid email format, password length, and password matching.
     */
    private boolean validateInputs() {
        boolean isValid = true;

        String fullName = fullNameEdit.getText().toString().trim();
        String email = emailEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String confirmPassword = confirmPasswordEdit.getText().toString().trim();

        // Name Validation
        if (fullName.isEmpty()) {
            fullNameInputLayout.setError(getString(R.string.error_empty_name));
            isValid = false;
        } else {
            fullNameInputLayout.setError(null);
        }

        // Email Validation
        if (email.isEmpty()) {
            emailInputLayout.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            emailInputLayout.setError(null);
        }

        // Password Validation
        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password_length));
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        // Confirm Password Validation
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.setError(getString(R.string.error_empty_confirm_password));
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordInputLayout.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        } else {
            confirmPasswordInputLayout.setError(null);
        }

        return isValid;
    }
}
