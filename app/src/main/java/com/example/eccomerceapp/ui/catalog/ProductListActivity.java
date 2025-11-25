package com.example.eccomerceapp.ui.catalog;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiMapper;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.repository.FavoritesRepository;
import com.example.eccomerceapp.data.repository.ProductRepository;
import com.example.eccomerceapp.databinding.ActivityProductListBinding;
import com.example.eccomerceapp.model.Product;
import com.example.eccomerceapp.ui.common.SpacingItemDecoration;
import com.example.eccomerceapp.ui.home.ProductAdapter;
import com.example.eccomerceapp.ui.product.ProductDetailActivity;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener {

    public static final String EXTRA_MODE = "extra_mode";
    public static final String EXTRA_CATEGORY_ID = "extra_category_id";
    public static final String EXTRA_TITLE = "extra_title";

    public static final String MODE_ALL = "mode_all";
    public static final String MODE_CATEGORY = "mode_category";
    public static final String MODE_FAVORITES = "mode_favorites";

    private ActivityProductListBinding binding;
    private ProductRepository productRepository;
    private FavoritesRepository favoritesRepository;
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private String mode = MODE_ALL;
    private long categoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.productToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.productToolbar.setNavigationOnClickListener(v -> finish());

        productRepository = new ProductRepository(this);
        favoritesRepository = new FavoritesRepository(this);
        apiService = ApiClient.getInstance();
        productAdapter = new ProductAdapter(this, this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        binding.productRecycler.setLayoutManager(gridLayoutManager);
        int spacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        binding.productRecycler.addItemDecoration(new SpacingItemDecoration(spacing, true));
        binding.productRecycler.setAdapter(productAdapter);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) {
            mode = MODE_ALL;
        }
        categoryId = getIntent().getLongExtra(EXTRA_CATEGORY_ID, -1);
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title == null) {
            title = getTitleForMode();
        }
        binding.productToolbar.setTitle(title);

        loadProducts();
    }

    private String getTitleForMode() {
        if (MODE_CATEGORY.equals(mode)) {
            return getString(R.string.section_category);
        } else if (MODE_FAVORITES.equals(mode)) {
            return getString(R.string.menu_likes);
        } else {
            return getString(R.string.all_products_title);
        }
    }

    private void loadProducts() {
        // For MODE_ALL (recommended products), fetch from API
        if (MODE_ALL.equals(mode)) {
            apiService.getProducts(null, null, null, null).enqueue(
                    new Callback<List<com.example.eccomerceapp.data.api.model.ApiProduct>>() {
                        @Override
                        public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call,
                                               Response<List<com.example.eccomerceapp.data.api.model.ApiProduct>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Product> products = ApiMapper.toProductList(response.body());
                                productAdapter.submitList(products);
                                binding.emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
                            } else {
                                Toast.makeText(ProductListActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                                binding.emptyView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call, Throwable t) {
                            Toast.makeText(ProductListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.emptyView.setVisibility(View.VISIBLE);
                        }
                    });
        } else if (MODE_FAVORITES.equals(mode)) {
            // For favorites, fetch all products from API and filter by favorites
            apiService.getProducts(null, null, null, null).enqueue(
                    new Callback<List<com.example.eccomerceapp.data.api.model.ApiProduct>>() {
                        @Override
                        public void onResponse(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call,
                                               Response<List<com.example.eccomerceapp.data.api.model.ApiProduct>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Product> allProducts = ApiMapper.toProductList(response.body());
                                // Filter to only show favorited products
                                List<Product> favoriteProducts = new java.util.ArrayList<>();
                                for (Product product : allProducts) {
                                    if (favoritesRepository.isFavorite(product.getId())) {
                                        favoriteProducts.add(product);
                                    }
                                }
                                productAdapter.submitList(favoriteProducts);
                                binding.emptyView.setVisibility(favoriteProducts.isEmpty() ? View.VISIBLE : View.GONE);
                            } else {
                                Toast.makeText(ProductListActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                                binding.emptyView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onFailure(Call<List<com.example.eccomerceapp.data.api.model.ApiProduct>> call, Throwable t) {
                            Toast.makeText(ProductListActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            binding.emptyView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            // For other modes (category), use local database
            List<Product> products;
            if (MODE_CATEGORY.equals(mode) && categoryId != -1) {
                products = productRepository.loadProducts(categoryId);
            } else {
                products = productRepository.loadProducts(null);
            }
            productAdapter.submitList(products);
            binding.emptyView.setVisibility(products.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onProductClicked(Product product) {
        android.content.Intent intent = new android.content.Intent(this, ProductDetailActivity.class);
        intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId());
        startActivity(intent);
    }
}

