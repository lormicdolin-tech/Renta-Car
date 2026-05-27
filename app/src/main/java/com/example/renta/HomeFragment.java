package com.example.renta;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment: The landing screen within HomeActivity.
 * Displays a list of available cars and handles clicks to view their details.
 */
public class HomeFragment extends Fragment {

    private ImageView bannerSlideshow;
    private final int[] carImages = {
            R.drawable.vios,
            R.drawable.mirage,
            R.drawable.innova,
            R.drawable.fortuner,
            R.drawable.wigo
    };
    private int currentImageIndex = 0;
    private Handler slideshowHandler;
    private Runnable slideshowRunnable;
    private List<View> carCards = new ArrayList<>();
    private List<String> carNames = new ArrayList<>();
    private List<String> carTypes = new ArrayList<>();
    private ChipGroup categoryGroup;
    private String currentSearchQuery = "";
    private String currentCategory = "All";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        bannerSlideshow = view.findViewById(R.id.banner_car_slideshow);
        setupSlideshow();

        // Initialize Search
        TextInputEditText searchInput = view.findViewById(R.id.search_input);
        LinearLayout contentLayout = (LinearLayout) view.findViewById(R.id.search_layout).getParent();

        // Clear previous data if fragment is recreated
        carCards.clear();
        carNames.clear();
        carTypes.clear();

        // Collect all car cards and their data for filtering
        for (int i = 0; i < contentLayout.getChildCount(); i++) {
            View child = contentLayout.getChildAt(i);
            if (child instanceof com.google.android.material.card.MaterialCardView) {
                // Skip the ad banner (it's the first card)
                if (child.findViewById(R.id.banner_car_slideshow) != null) continue;

                carCards.add(child);
                
                try {
                    // Correct extraction for MaterialCardView -> LinearLayout (vertical) -> LinearLayout (horizontal info container)
                    // The car cards in fragment_home.xml have:
                    // Card -> LinearLayout (Vertical)
                    //   -> ImageView (index 0)
                    //   -> LinearLayout (Vertical, index 1)
                    //     -> TextView name (index 0)
                    //     -> TextView desc (index 1)
                    //     -> LinearLayout price/btn (index 2)
                    
                    LinearLayout cardContent = (LinearLayout) ((com.google.android.material.card.MaterialCardView) child).getChildAt(0);
                    LinearLayout textContainer = (LinearLayout) cardContent.getChildAt(1);
                    android.widget.TextView nameTv = (android.widget.TextView) textContainer.getChildAt(0);
                    android.widget.TextView descTv = (android.widget.TextView) textContainer.getChildAt(1);

                    carNames.add(nameTv.getText().toString().toLowerCase());
                    carTypes.add(descTv.getText().toString().toLowerCase());
                } catch (Exception e) {
                    // Fallback if structure differs
                    carNames.add("");
                    carTypes.add("");
                }
            }
        }

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize Category Chips
        categoryGroup = view.findViewById(R.id.category_chip_group);
        categoryGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentCategory = "All";
            } else {
                Chip chip = view.findViewById(checkedIds.get(0));
                currentCategory = chip.getText().toString();
            }
            applyFilters();
        });

        // List of button IDs for car detail buttons
        int[] buttonIds = {
            R.id.car_1_details_btn,
            R.id.car_2_details_btn,
            R.id.car_3_details_btn,
            R.id.car_4_details_btn,
            R.id.car_5_details_btn
        };

        // Common click listener to handle car selection
        View.OnClickListener detailsClickListener = v -> {
            Intent intent = new Intent(getActivity(), CarDetailsActivity.class);
            
            // Pass specific car data based on which button was clicked
            int id = v.getId();
            if (id == R.id.car_1_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_1_name));
                intent.putExtra("car_price", getString(R.string.car_1_price));
                intent.putExtra("car_desc", getString(R.string.car_1_desc));
                intent.putExtra("car_image", R.drawable.vios);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_2_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_2_name));
                intent.putExtra("car_price", getString(R.string.car_2_price));
                intent.putExtra("car_desc", getString(R.string.car_2_desc));
                intent.putExtra("car_image", R.drawable.mirage);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Manual/CVT");
                intent.putExtra("car_cond", "Excellent");
            } else if (id == R.id.car_3_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_3_name));
                intent.putExtra("car_price", getString(R.string.car_3_price));
                intent.putExtra("car_desc", getString(R.string.car_3_desc));
                intent.putExtra("car_image", R.drawable.innova);
                intent.putExtra("car_fuel", "Diesel");
                intent.putExtra("car_seats", "7 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_4_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_4_name));
                intent.putExtra("car_price", getString(R.string.car_4_price));
                intent.putExtra("car_desc", getString(R.string.car_4_desc));
                intent.putExtra("car_image", R.drawable.fortuner);
                intent.putExtra("car_fuel", "Diesel");
                intent.putExtra("car_seats", "7 Persons");
                intent.putExtra("car_trans", "Automatic");
                intent.putExtra("car_cond", "Pristine");
            } else if (id == R.id.car_5_details_btn) {
                intent.putExtra("car_name", getString(R.string.car_5_name));
                intent.putExtra("car_price", getString(R.string.car_5_price));
                intent.putExtra("car_desc", getString(R.string.car_5_desc));
                intent.putExtra("car_image", R.drawable.wigo);
                intent.putExtra("car_fuel", "Gasoline");
                intent.putExtra("car_seats", "5 Persons");
                intent.putExtra("car_trans", "Manual");
                intent.putExtra("car_cond", "Excellent");
            }

            // Start the details activity
            startActivity(intent);
        };

        // Assign the listener to all car detail buttons
        for (int id : buttonIds) {
            MaterialButton btn = view.findViewById(id);
            if (btn != null) {
                btn.setOnClickListener(detailsClickListener);
            }
        }

        // Link the "BOOK NOW" banner button to car 1 (Vios) as a default action
        MaterialButton bookNowBannerBtn = view.findViewById(R.id.book_now_banner_btn);
        if (bookNowBannerBtn != null) {
            bookNowBannerBtn.setOnClickListener(v -> {
                // For now, let's just trigger the same details view as Car 1
                view.findViewById(R.id.car_1_details_btn).performClick();
            });
        }

        return view;
    }

    private void applyFilters() {
        String query = currentSearchQuery.toLowerCase().trim();
        for (int i = 0; i < carCards.size(); i++) {
            boolean matchesSearch = carNames.get(i).contains(query) || carTypes.get(i).contains(query);
            boolean matchesCategory = currentCategory.equals("All") || carTypes.get(i).contains(currentCategory.toLowerCase());
            
            carCards.get(i).setVisibility(matchesSearch && matchesCategory ? View.VISIBLE : View.GONE);
        }
    }

    private void filterCars(String query) {
        currentSearchQuery = query;
        applyFilters();
    }

    private void setupSlideshow() {
        slideshowHandler = new Handler(Looper.getMainLooper());
        slideshowRunnable = new Runnable() {
            @Override
            public void run() {
                if (bannerSlideshow != null && isAdded()) {
                    // Smooth transition animation
                    bannerSlideshow.animate()
                            .alpha(0f)
                            .setDuration(1000)
                            .withEndAction(() -> {
                                if (isAdded() && bannerSlideshow != null) {
                                    currentImageIndex = (currentImageIndex + 1) % carImages.length;
                                    bannerSlideshow.setImageResource(carImages[currentImageIndex]);
                                    bannerSlideshow.animate()
                                            .alpha(0.8f)
                                            .setDuration(1000)
                                            .start();
                                }
                            }).start();
                }
                slideshowHandler.postDelayed(this, 3000); // Change every 3 seconds
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        if (slideshowHandler != null) {
            slideshowHandler.postDelayed(slideshowRunnable, 3000);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (slideshowHandler != null) {
            slideshowHandler.removeCallbacks(slideshowRunnable);
        }
    }
}
