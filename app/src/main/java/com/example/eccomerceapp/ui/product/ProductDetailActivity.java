package com.example.eccomerceapp.ui.product;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiMapper;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.databinding.ActivityProductDetailBinding;
import com.example.eccomerceapp.databinding.DialogImageZoomBinding;
import com.example.eccomerceapp.model.Product;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ActivityProductDetailBinding binding;
    private ApiService apiService;
    private CartRepository cartRepository;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.detailToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.detailToolbar.setNavigationOnClickListener(v -> finish());

        apiService = ApiClient.getInstance();
        cartRepository = new CartRepository(this);

        long productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            finish();
            return;
        }

        loadProduct(productId);
        binding.buttonAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct(long productId) {
        apiService.getProductById(productId).enqueue(new Callback<ApiProduct>() {
            @Override
            public void onResponse(Call<ApiProduct> call, Response<ApiProduct> response) {
                if (response.isSuccessful() && response.body() != null) {
                    product = ApiMapper.toProduct(response.body());
                    bindProductData();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Product not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiProduct> call, Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load product: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
    
    private void bindProductData() {
        if (product == null) return;
        
        binding.detailProductName.setText(product.getName());
        binding.detailProductPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", product.getPrice()));
        binding.detailProductDescription.setText(product.getDescription());

        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .centerCrop()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(binding.productHeroImage);
        } else {
            binding.productHeroImage.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        List<String> sizes = product.getSizes();
        List<String> colors = product.getColors();
        
        // Only show sizes if they exist
        if (sizes != null && !sizes.isEmpty()) {
            binding.sizeSection.setVisibility(View.VISIBLE);
            populateChipGroup(binding.sizeChipGroup, sizes);
        } else {
            binding.sizeSection.setVisibility(View.GONE);
        }
        
        // Only show colors if they exist
        if (colors != null && !colors.isEmpty()) {
            binding.colorSection.setVisibility(View.VISIBLE);
            populateChipGroup(binding.colorChipGroup, colors);
        } else {
            binding.colorSection.setVisibility(View.GONE);
        }
        
        // Hide store section (not in admin panel)
        binding.storeSection.setVisibility(View.GONE);
        
        // Set image click listener
        binding.productHeroImage.setOnClickListener(v -> showImageZoom(imageUrl));
    }

    private void populateChipGroup(com.google.android.material.chip.ChipGroup chipGroup, List<String> values) {
        chipGroup.removeAllViews();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Chip chip = new Chip(this);
            chip.setText(value);
            chip.setCheckable(true);
            chip.setClickable(true);
            
            // Style as boxes with faded background when selected
            // Create ColorStateList programmatically for background
            int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
            };
            int[] colors = new int[]{
                Color.parseColor("#80FF6B3D"), // Faded orange when selected (50% opacity)
                Color.parseColor("#F4F4F6") // Light gray when not selected
            };
            chip.setChipBackgroundColor(new ColorStateList(states, colors));
            
            // Create ColorStateList for stroke
            int[] strokeColors = new int[]{
                Color.parseColor("#FF6B3D"), // Orange when selected
                Color.parseColor("#9E9E9E") // Gray when not selected
            };
            chip.setChipStrokeColor(new ColorStateList(states, strokeColors));
            chip.setChipStrokeWidth(getResources().getDisplayMetrics().density); // 1dp in pixels
            
            // Create ColorStateList for text
            int[] textColors = new int[]{
                Color.parseColor("#FF6B3D"), // Orange when selected
                Color.parseColor("#1B1B1D") // Dark when not selected
            };
            chip.setTextColor(new ColorStateList(states, textColors));
            
            // Set dimensions directly in pixels (48dp = 48 * density, 8dp = 8 * density)
            float density = getResources().getDisplayMetrics().density;
            chip.setChipMinHeight((int)(48 * density));
            // Set padding to control width indirectly
            chip.setPadding((int)(16 * density), (int)(12 * density), (int)(16 * density), (int)(12 * density));
            chip.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
            chip.setEnsureMinTouchTargetSize(false);
            chip.setChipCornerRadius((int)(8 * density));
            
            chipGroup.addView(chip);
            if (i == 0) {
                chip.setChecked(true);
            }
        }
    }

    private void addToCart() {
        String selectedSize = getSelectedChipText(binding.sizeChipGroup);
        String selectedColor = getSelectedChipText(binding.colorChipGroup);
        cartRepository.addToCart(product.getId(), 1, selectedSize, selectedColor);
        Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
    }

    private String getSelectedChipText(com.google.android.material.chip.ChipGroup chipGroup) {
        int checkedId = chipGroup.getCheckedChipId();
        if (checkedId == -1) {
            return null;
        }
        Chip chip = chipGroup.findViewById(checkedId);
        return chip != null ? chip.getText().toString() : null;
    }
    
    private void showImageZoom(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        DialogImageZoomBinding dialogBinding = DialogImageZoomBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());
        
        // Load image
        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .into(dialogBinding.zoomImageView);
        
        // Close button
        dialogBinding.closeButton.setOnClickListener(v -> dialog.dismiss());
        
        // Click outside to close
        dialogBinding.zoomImageView.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
}

