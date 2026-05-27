package com.example.renta;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.FirebaseDatabase;

/**
 * MainActivity: The entry point of the application.
 * Handles user login, specific Admin access, and Guest entry.
 */
public class MainActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout, passwordInputLayout;
    private TextInputEditText nameEdit, passwordEdit;
    private MaterialButton loginBtn, guestLoginBtn;
    private TextView signupBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        nameInputLayout = findViewById(R.id.nameInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        nameEdit = findViewById(R.id.nameEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        loginBtn = findViewById(R.id.loginBtn);
        guestLoginBtn = findViewById(R.id.guestLoginBtn);
        signupBtn = findViewById(R.id.signupBtn);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Regular Login Button
        loginBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = nameEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();

                if (email.equalsIgnoreCase("admin@renta.com")) {
                    handleAdminLogin(email, password);
                } else {
                    handleUserLogin(email, password);
                }
            }
        });

        // Guest Login Button
        guestLoginBtn.setOnClickListener(v -> handleGuestLogin());

        // Development/Quick Admin Button removed as requested
        /*
        if (adminLoginBtn != null) {
            adminLoginBtn.setOnClickListener(v -> handleAdminLogin("admin@renta.com", "admin123"));
        }
        */

        signupBtn.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
        });
    }

    private void handleAdminLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Update admin flag in database on every successful login to be safe
                    String uid = mAuth.getCurrentUser().getUid();
                    FirebaseConfig.getDatabase().getReference().child("users")
                        .child(uid).child("isAdmin").setValue(true);
                    proceedToAdmin();
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        // Admin doesn't exist, create it
                        mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, createStack -> {
                                if (createStack.isSuccessful()) {
                                    String uid = mAuth.getCurrentUser().getUid();
                                    FirebaseConfig.getDatabase().getReference().child("users")
                                        .child(uid).child("isAdmin").setValue(true);
                                    proceedToAdmin();
                                } else {
                                    Toast.makeText(MainActivity.this, "Admin Setup Failed: " + createStack.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    } else {
                        Toast.makeText(MainActivity.this, "Admin Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    private void handleUserLogin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    proceedToHome("Welcome to RentA!");
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void handleGuestLogin() {
        // Try Anonymous Login first (best for unique user IDs)
        mAuth.signInAnonymously()
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    proceedToHome("Logged in as Guest");
                } else {
                    // If Anonymous fails, try the shared guest account as backup
                    String guestEmail = "guest@renta.com";
                    String guestPass = "guest123";

                    mAuth.signInWithEmailAndPassword(guestEmail, guestPass)
                        .addOnCompleteListener(this, loginTask -> {
                            if (loginTask.isSuccessful()) {
                                proceedToHome("Browsing as Guest");
                            } else {
                                // Try to create the shared guest account if it doesn't exist
                                mAuth.createUserWithEmailAndPassword(guestEmail, guestPass)
                                    .addOnCompleteListener(this, createStack -> {
                                        if (createStack.isSuccessful()) {
                                            proceedToHome("Guest session started");
                                        } else {
                                            String errorMsg = task.getException() != null ? task.getException().getMessage() : "Connection Error";
                                            android.util.Log.e("RentA_Login", "Guest login failed: " + errorMsg);
                                            Toast.makeText(MainActivity.this, "Guest access unavailable: " + errorMsg, Toast.LENGTH_LONG).show();
                                        }
                                    });
                            }
                        });
                }
            });
    }

    private void proceedToAdmin() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("auto_login", true).putBoolean("is_admin", true).apply();

        Toast.makeText(this, "Admin Mode Active", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, AdminActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void proceedToHome(String msg) {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("auto_login", true).putBoolean("is_admin", false).apply();

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    private boolean validateInputs() {
        String email = nameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        boolean valid = true;

        if (email.isEmpty()) {
            nameInputLayout.setError("Email is required");
            valid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            nameInputLayout.setError("Invalid email format");
            valid = false;
        } else {
            nameInputLayout.setError(null);
        }

        if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            valid = false;
        } else {
            passwordInputLayout.setError(null);
        }
        return valid;
    }
}
