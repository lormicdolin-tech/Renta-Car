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

/**
 * MainActivity: Handles user login (Local & Guest).
 */
public class MainActivity extends AppCompatActivity {

    private TextInputLayout nameInputLayout, passwordInputLayout;
    private TextInputEditText nameEdit, passwordEdit;
    private MaterialButton loginBtn, guestLoginBtn;
    private TextView signupBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

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

        loginBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                String email = nameEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();

                UserManager userManager = new UserManager(this);
                User savedUser = userManager.getUser();

                if (email.equals(savedUser.getEmail()) && password.equals(savedUser.getPassword())) {
                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        guestLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        signupBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void applyDarkMode() {
        SharedPreferences prefs = getSharedPreferences("renta_prefs", Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private boolean validateInputs() {
        boolean isValid = true;
        String username = nameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (username.isEmpty()) {
            nameInputLayout.setError(getString(R.string.error_empty_email));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            nameInputLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        } else {
            nameInputLayout.setError(null);
        }

        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.error_empty_password));
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password_length));
            isValid = false;
        } else {
            passwordInputLayout.setError(null);
        }

        return isValid;
    }
}
