package com.example.eccomerceapp.ui.product;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.data.repository.ProductRepository;
import com.example.eccomerceapp.databinding.ActivityProductDetailBinding;
import com.example.eccomerceapp.model.Product;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCT_ID = "extra_product_id";

    private ActivityProductDetailBinding binding;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private Product product;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.detailToolbar);
        binding.detailToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);

        long productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        if (productId == -1) {
            finish();
            return;
        }

        product = productRepository.findProduct(productId);
        if (product == null) {
            finish();
            return;
        }

        bindProductData();
        binding.buttonAddToCart.setOnClickListener(v -> addToCart());
    }

    private void bindProductData() {
        binding.detailProductName.setText(product.getName());
        binding.detailProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
        binding.detailProductDescription.setText(product.getDescription());

        Glide.with(this)
                .load(product.getImageUrl())
                .centerCrop()
                .into(binding.productHeroImage);

        populateChipGroup(binding.sizeChipGroup, product.getSizes());
        populateChipGroup(binding.colorChipGroup, product.getColors());
    }

    private void populateChipGroup(com.google.android.material.chip.ChipGroup chipGroup, List<String> values) {
        chipGroup.removeAllViews();
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i);
            Chip chip = new Chip(this);
            chip.setText(value);
            chip.setCheckable(true);
            chip.setClickable(true);
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
}

