package com.example.renta;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

/**
 * HomeActivity: The main dashboard of the app after login.
 * Manages the Side Navigation Drawer and Bottom Navigation View to switch between 
 * different fragments (Home, Bookings, Profile, Location, Settings).
 */
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private UserManager userManager;
    private TextView navHeaderName;
    private TextView navHeaderEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize UI components for navigation
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);

        // Set up the toolbar as the app bar
        setSupportActionBar(toolbar);

        userManager = new UserManager(this);
        
        // Initialize Navigation Header views (Name and Email)
        View headerView = navigationView.getHeaderView(0);
        navHeaderName = headerView.findViewById(R.id.nav_header_name);
        navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        updateNavHeader();

        navigationView.setNavigationItemSelectedListener(this);

        // Set up the Hamburger menu icon for the drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.nav_home, R.string.nav_home);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle back button behavior (close drawer if open)
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

        // Set default fragment to Home on first launch
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // Handle Bottom Navigation clicks
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            // Sync side drawer selection with bottom nav selection
            navigationView.setCheckedItem(itemId);
            
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_home);
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
        // Refresh header info whenever activity returns to focus
        updateNavHeader();
    }

    /**
     * Updates the navigation drawer header with current user information.
     */
    private void updateNavHeader() {
        User user = userManager.getUser();
        if (navHeaderName != null) navHeaderName.setText(user.getName());
        if (navHeaderEmail != null) navHeaderEmail.setText(user.getEmail());
    }

    /**
     * Handles selection of items in the Side Navigation Drawer.
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (itemId == R.id.nav_home || itemId == R.id.nav_bookings || itemId == R.id.nav_profile || itemId == R.id.nav_location) {
            // Selecting these will sync with the bottom navigation
            bottomNavigationView.setSelectedItemId(itemId);
        } else if (itemId == R.id.nav_settings) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new SettingsFragment()).commit();
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.nav_settings);
        } else if (itemId == R.id.nav_logout) {
            // Exit the activity
            finish();
        }

        // Close the drawer after an item is selected
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
