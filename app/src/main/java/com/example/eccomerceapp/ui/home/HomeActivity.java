package com.example.eccomerceapp.ui.home;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.data.repository.ProductRepository;
import com.example.eccomerceapp.databinding.ActivityHomeBinding;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Product;
import com.example.eccomerceapp.ui.cart.CartActivity;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;
import com.example.eccomerceapp.ui.catalog.CategoryListActivity;
import com.example.eccomerceapp.ui.catalog.ProductListActivity;
import com.example.eccomerceapp.ui.common.SpacingItemDecoration;
import com.example.eccomerceapp.ui.product.ProductDetailActivity;
import com.example.eccomerceapp.ui.profile.ProfileActivity;
import com.example.eccomerceapp.ui.wallet.WalletActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements
        CategoryAdapter.OnCategoryClickListener,
        ProductAdapter.OnProductClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private ActivityHomeBinding binding;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private Long selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productRepository = new ProductRepository(this);
        cartRepository = new CartRepository(this);
        sessionManager = new SessionManager(this);

        setupDrawerHeader();
        setupRecyclerViews();
        setupListeners();

        loadCategories();
        loadProducts(null);
        updateCartBadge();
    }

    private void setupDrawerHeader() {
        View header = binding.navigationView.getHeaderView(0);
        TextView headerName = header.findViewById(R.id.headerName);
        headerName.setText(sessionManager.getUserName());
        binding.textUserName.setText(sessionManager.getUserName());
        binding.navigationView.setNavigationItemSelectedListener(this);
        startProfileAnimations(header);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this);
        binding.categoryRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecycler.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.productRecycler.setLayoutManager(gridLayoutManager);
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        binding.productRecycler.addItemDecoration(new SpacingItemDecoration(spacing, true));
        binding.productRecycler.setAdapter(productAdapter);
    }

    private void setupListeners() {
        binding.buttonMenu.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.START));
        binding.buttonCart.setOnClickListener(v -> navigateToCart());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.menu_home) {
                return true;
            } else if (item.getItemId() == R.id.menu_cart) {
                navigateToCart();
                return true;
            } else if (item.getItemId() == R.id.menu_wallet) {
                startActivity(new Intent(this, WalletActivity.class));
                return true;
            } else if (item.getItemId() == R.id.menu_profile) {
                animateDrawable(item.getIcon());
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            } else {
                return false;
            }
        });
        binding.bottomNavigation.setSelectedItemId(R.id.menu_home);

        binding.inputSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(v.getText().toString());
                return true;
            }
            return false;
        });

        binding.buttonSeeMoreCategory.setOnClickListener(v ->
                startActivity(new Intent(this, CategoryListActivity.class)));
        binding.buttonSeeMoreProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_ALL);
            intent.putExtra(ProductListActivity.EXTRA_TITLE, getString(R.string.section_recommended));
            startActivity(intent);
        });
    }

    private void loadCategories() {
        List<Category> categories = productRepository.loadCategories();
        categoryAdapter.submitList(categories);
    }

    private void loadProducts(Long categoryId) {
        List<Product> products = productRepository.loadProducts(categoryId);
        productAdapter.submitList(products);
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadProducts(selectedCategoryId);
            return;
        }
        List<Product> results = productRepository.searchProducts(keyword);
        productAdapter.submitList(results);
    }

    private void navigateToCart() {
        startActivity(new Intent(this, CartActivity.class));
    }

    @Override
    public void onCategorySelected(Category category) {
        selectedCategoryId = category.getId();
        loadProducts(selectedCategoryId);
    }

    @Override
    public void onProductClicked(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.drawer_logout) {
            sessionManager.logOut();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (item.getItemId() == R.id.drawer_home) {
            return true;
        } else if (item.getItemId() == R.id.drawer_orders) {
            startActivity(new Intent(this, OrderHistoryActivity.class));
            return true;
        } else if (item.getItemId() == R.id.drawer_category) {
            startActivity(new Intent(this, CategoryListActivity.class));
            return true;
        } else if (item.getItemId() == R.id.drawer_likes) {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_FAVORITES);
            intent.putExtra(ProductListActivity.EXTRA_TITLE, getString(R.string.menu_likes));
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.drawer_settings) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts(selectedCategoryId);
        updateCartBadge();
    }

    private void startProfileAnimations(View header) {
        MenuItem profileItem = binding.bottomNavigation.getMenu().findItem(R.id.menu_profile);
        animateDrawable(profileItem.getIcon());
        ImageView avatar = header.findViewById(R.id.headerAvatar);
        if (avatar != null) {
            animateDrawable(avatar.getDrawable());
        }
    }

    private void animateDrawable(Drawable drawable) {
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }

    private void updateCartBadge() {
        int count = cartRepository.getCartCount();
        if (count > 0) {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.menu_cart);
            badge.setVisible(true);
            badge.setNumber(count);
            badge.setBackgroundColor(getColor(R.color.primary_orange));
            badge.setBadgeTextColor(getColor(android.R.color.white));
            binding.cartBadgeText.setVisibility(View.VISIBLE);
            binding.cartBadgeText.setText(String.valueOf(count));
        } else {
            binding.bottomNavigation.removeBadge(R.id.menu_cart);
            binding.cartBadgeText.setVisibility(View.GONE);
        }
    }
}
