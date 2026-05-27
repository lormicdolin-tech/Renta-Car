package com.example.renta;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * HomeActivity: The main navigation hub for the application.
 * It manages the side navigation drawer and bottom navigation view, switching between
 * different app features via Fragments.
 */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private UserManager userManager;
    private TextView navHeaderName;
    private TextView navHeaderEmail;
    private ImageView navHeaderImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyDarkMode();
        setContentView(R.layout.activity_home);

        // Initialize UI components for navigation controls
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        // Set up the toolbar as the primary action bar
        setSupportActionBar(toolbar);

        userManager = new UserManager(this);
        
        // Setup Navigation Header views (to show Name and Email)
        View headerView = navigationView.getHeaderView(0);
        navHeaderName = headerView.findViewById(R.id.nav_header_name);
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        navHeaderImage = headerView.findViewById(R.id.nav_header_image);
        updateNavHeader();

        navigationView.setNavigationItemSelectedListener(this);

        // Dynamically check if the user has admin privileges to display Admin-only menus
        checkAdminStatus(navigationView);

        // Configure the drawer toggle (Hamburger menu icon)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_home, R.string.nav_home);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle back button behavior: close the navigation drawer if it's open
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Load the appropriate fragment based on Intent or default to HomeFragment
        if (savedInstanceState == null) {
            String navigateTo = getIntent().getStringExtra("navigate_to");
            if ("bookings".equals(navigateTo)) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new BookingsFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_bookings);
                bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_bookings);
            } else {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HomeFragment()).commit();
                navigationView.setCheckedItem(R.id.nav_home);
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }

        // Handle Bottom Navigation item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            // Keep side drawer selection in sync with bottom nav
            navigationView.setCheckedItem(itemId);
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                updateHomeTitle();
            } else if (itemId == R.id.nav_bookings) {
                selectedFragment = new BookingsFragment();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_bookings);
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_profile);
            } else if (itemId == R.id.nav_location) {
                selectedFragment = new LocationFragment();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_location);
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
            }
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the header info is up-to-date
        updateNavHeader();
    }

    /**
     * checkAdminStatus: Checks Firebase Database to see if the current UID has 'isAdmin' set to true.
     * If so, reveals the "Admin Dashboard" menu item.
     */
    private void checkAdminStatus(NavigationView navigationView) {
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !firebaseUser.isAnonymous()) {
            FirebaseConfig.getDatabase()
                .getReference("users").child(firebaseUser.getUid())
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null && user.isAdmin()) {
                            navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {}
                });
        }
    }

    /**
     * updateNavHeader: Updates the navigation drawer with user details.
     * Distinguishes between registered users and anonymous guests.
     */
    private void updateNavHeader() {
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                if (navHeaderName != null) navHeaderName.setText("Guest User");
                if (navHeaderEmail != null) navHeaderEmail.setText("Anonymous Account");
                if (navHeaderImage != null) navHeaderImage.setImageResource(R.drawable.logo);
            } else {
                String name = firebaseUser.getDisplayName();
                String email = firebaseUser.getEmail();
                
                if (name == null || name.isEmpty()) {
                    User user = userManager.getUser();
                    name = user.getName();
                }
                
                if (navHeaderName != null) navHeaderName.setText(name);
                if (navHeaderEmail != null) navHeaderEmail.setText(email);
                
                if (navHeaderImage != null) {
                    android.net.Uri photoUrl = firebaseUser.getPhotoUrl();
                    if (photoUrl != null) {
                        Glide.with(this).load(photoUrl).placeholder(R.drawable.logo).into(navHeaderImage);
                    } else {
                        navHeaderImage.setImageResource(R.drawable.logo);
                    }
                }
            }
        }
        
        // Update Toolbar title if Home is selected
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null && bottomNavigationView.getSelectedItemId() == R.id.nav_home) {
            updateHomeTitle();
        }
    }

    /**
     * updateHomeTitle: Updates the toolbar title with the user's first name
     * when on the home screen.
     */
    private void updateHomeTitle() {
        if (getSupportActionBar() == null) return;
        
        com.google.firebase.auth.FirebaseUser firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            if (firebaseUser.isAnonymous()) {
                getSupportActionBar().setTitle("Guest");
            } else {
                String name = firebaseUser.getDisplayName();
                if (name == null || name.isEmpty()) {
                    User user = userManager.getUser();
                    name = user.getName();
                }
                
                String firstName = name;
                if (firstName != null && firstName.contains(" ")) {
                    firstName = firstName.split(" ")[0];
                }
                getSupportActionBar().setTitle(firstName);
            }
        } else {
            getSupportActionBar().setTitle(R.string.nav_home);
        }
    }

    /**
     * onNavigationItemSelected: Handles click events for side drawer items.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (itemId == R.id.nav_home || itemId == R.id.nav_bookings || itemId == R.id.nav_profile || itemId == R.id.nav_location) {
            // Forward main navigation items to the bottom navigation handler
            bottomNavigationView.setSelectedItemId(itemId);
        } else if (itemId == R.id.nav_notifications) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new NotificationsFragment()).commit();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("Notifications");
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment()).commit();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_settings);
        } else if (itemId == R.id.nav_admin) {
            // Open the Admin panel
            android.content.Intent intent = new android.content.Intent(this, AdminActivity.class);
            startActivity(intent);
        } else if (itemId == R.id.nav_logout) {
            // Disable auto-login on manual logout
            android.content.SharedPreferences prefs = getSharedPreferences("renta_prefs", android.content.Context.MODE_PRIVATE);
            prefs.edit().putBoolean("auto_login", false).apply();

            // Perform Firebase sign-out and return to login screen
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            android.content.Intent intent = new android.content.Intent(this, MainActivity.class);
            // Clear backstack to prevent returning to home screen via back button
            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Close side drawer after selection
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void applyDarkMode() {
        android.content.SharedPreferences prefs = getSharedPreferences("renta_prefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
