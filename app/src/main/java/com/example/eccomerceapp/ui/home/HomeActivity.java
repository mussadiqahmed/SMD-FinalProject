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
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiMapper;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.data.repository.FavoritesRepository;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityHomeBinding;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Product;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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
    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private ApiService apiService;

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private Long selectedCategoryId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartRepository = new CartRepository(this);
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getInstance();

        setupDrawerHeader();
        setupRecyclerViews();
        setupListeners();

        // Clear all cart entries immediately
        cartRepository.clearAllCartEntries();
        // Also clean up orphaned cart entries (products deleted from admin panel)
        cartRepository.cleanupOrphanedEntries();
        
        // Clean up orphaned favorites (products deleted from admin panel)
        FavoritesRepository favoritesRepository = new FavoritesRepository(this);
        favoritesRepository.cleanupOrphanedFavorites();
        
        // Clear all orders from local database immediately
        OrderRepository orderRepository = new OrderRepository(this);
        orderRepository.clearAllOrders();

        loadCategories();
        loadAllProductsForRecommended();
        // Force update cart badge after clearing
        updateCartBadge();
    }

    private void setupDrawerHeader() {
        View header = binding.navigationView.getHeaderView(0);
        TextView headerName = header.findViewById(R.id.headerName);
        TextView headerEmail = header.findViewById(R.id.headerEmail);
        String userName = sessionManager.getUserName();
        String userEmail = sessionManager.getUserEmail();
        headerName.setText(userName);
        headerEmail.setText(userEmail);
        binding.textUserName.setText(userName);
        binding.navigationView.setNavigationItemSelectedListener(this);
        startProfileAnimations(header);
    }

    private void setupRecyclerViews() {
        categoryAdapter = new CategoryAdapter(this);
        binding.categoryRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecycler.setAdapter(categoryAdapter);

        productAdapter = new ProductAdapter(this, this);
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

        // Category "See More" button is hidden, so no listener needed
        binding.buttonSeeMoreProduct.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_ALL);
            intent.putExtra(ProductListActivity.EXTRA_TITLE, getString(R.string.section_recommended));
            startActivity(intent);
        });
    }

    private void loadCategories() {
        apiService.getCategories().enqueue(new Callback<List<com.example.eccomerceapp.data.api.model.ApiCategory>>() {
            @Override
            public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiCategory>> call,
                                   Response<List<com.example.eccomerceapp.data.api.model.ApiCategory>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = ApiMapper.toCategoryList(response.body());
                    categoryAdapter.submitList(categories);
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiCategory>> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllProductsForRecommended() {
        // Load all products randomly for recommended section
        apiService.getProducts(null, null, null, null).enqueue(
                new Callback<List<com.example.eccomerceapp.data.api.model.ApiProduct>>() {
                    @Override
                    public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call,
                                           Response<List<com.example.eccomerceapp.data.api.model.ApiProduct>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Shuffle products for random display
                            List<Product> products = ApiMapper.toProductListShuffled(response.body());
                            productAdapter.submitList(products);
                        } else {
                            Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    
    private void loadProducts(Long categoryId) {
        if (categoryId == null) {
            loadAllProductsForRecommended();
            return;
        }
        
        apiService.getProducts(categoryId, null, null, null).enqueue(
                new Callback<List<com.example.eccomerceapp.data.api.model.ApiProduct>>() {
                    @Override
                    public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call,
                                           Response<List<com.example.eccomerceapp.data.api.model.ApiProduct>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> products = ApiMapper.toProductList(response.body());
                            productAdapter.submitList(products);
                        } else {
                            Toast.makeText(HomeActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void performSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            if (selectedCategoryId != null) {
                loadProducts(selectedCategoryId);
            } else {
                loadAllProductsForRecommended();
            }
            return;
        }
        // Search across all products regardless of category filter
        apiService.getProducts(null, null, keyword, null).enqueue(
                new Callback<List<com.example.eccomerceapp.data.api.model.ApiProduct>>() {
                    @Override
                    public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call,
                                           Response<List<com.example.eccomerceapp.data.api.model.ApiProduct>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Product> results = ApiMapper.toProductList(response.body());
                            productAdapter.submitList(results);
                        } else {
                            Toast.makeText(HomeActivity.this, "Search failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call, Throwable t) {
                        Toast.makeText(HomeActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToCart() {
        startActivity(new Intent(this, CartActivity.class));
    }

    @Override
    public void onCategorySelected(Category category) {
        selectedCategoryId = category.getId();
        loadProducts(category.getId());
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
        if (selectedCategoryId != null) {
            loadProducts(selectedCategoryId);
        } else {
            loadAllProductsForRecommended();
        }
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
