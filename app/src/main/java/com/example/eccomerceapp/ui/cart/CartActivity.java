package com.example.eccomerceapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiMapper;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.databinding.ActivityCartBinding;
import com.example.eccomerceapp.model.CartItem;
import com.example.eccomerceapp.model.Product;
import com.example.eccomerceapp.data.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {

    private ActivityCartBinding binding;
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
    private ApiService apiService;
    private double currentTotal = 0d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.cartToolbar);
        binding.cartToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        cartRepository = new CartRepository(this);
        cartAdapter = new CartAdapter(this);
        apiService = ApiClient.getInstance();

        binding.cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.cartRecycler.setAdapter(cartAdapter);

        binding.buttonCheckout.setOnClickListener(v -> openCheckout());

        loadCartItems();
    }

    private void openCheckout() {
        if (currentTotal <= 0d) {
            Toast.makeText(this, "Add items to your cart first", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, CheckoutActivity.class);
        intent.putExtra(CheckoutActivity.EXTRA_ORDER_TOTAL, currentTotal);
        startActivity(intent);
    }

    private void loadCartItems() {
        // Get raw cart data from local DB (productId, quantity, size, color)
        List<com.example.eccomerceapp.data.local.AppDatabaseHelper.CartRawData> cartDataList = cartRepository.getCartRawData();
        
        if (cartDataList == null || cartDataList.isEmpty()) {
            cartAdapter.submitList(new ArrayList<>());
            binding.cartEmptyView.setVisibility(View.VISIBLE);
            binding.cartRecycler.setVisibility(View.GONE);
            binding.cartTotalPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", 0.0));
            return;
        }

        // Fetch product details from API for each cart item
        List<CartItem> cartItems = new ArrayList<>();
        final int[] loadedCount = {0};
        final int totalItems = cartDataList.size();

        for (com.example.eccomerceapp.data.local.AppDatabaseHelper.CartRawData cartData : cartDataList) {
            final com.example.eccomerceapp.data.local.AppDatabaseHelper.CartRawData data = cartData; // Final reference for closure
            apiService.getProductById(data.productId).enqueue(new Callback<ApiProduct>() {
                @Override
                public void onResponse(Call<ApiProduct> call, Response<ApiProduct> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = ApiMapper.toProduct(response.body());
                        CartItem item = new CartItem(
                            data.cartId,
                            product,
                            data.quantity,
                            data.size != null ? data.size : "",
                            data.color != null ? data.color : ""
                        );
                        synchronized (cartItems) {
                            cartItems.add(item);
                            loadedCount[0]++;
                            
                            if (loadedCount[0] == totalItems) {
                                // All products loaded, update UI
                                runOnUiThread(() -> {
                                    cartAdapter.submitList(new ArrayList<>(cartItems));
                                    binding.cartEmptyView.setVisibility(View.GONE);
                                    binding.cartRecycler.setVisibility(View.VISIBLE);
                                    
                                    currentTotal = 0;
                                    for (CartItem cartItem : cartItems) {
                                        currentTotal += cartItem.getTotalPrice();
                                    }
                                    binding.cartTotalPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", currentTotal));
                                });
                            }
                        }
                    } else {
                        loadedCount[0]++;
                        if (loadedCount[0] == totalItems) {
                            runOnUiThread(() -> {
                                if (cartItems.isEmpty()) {
                                    binding.cartEmptyView.setVisibility(View.VISIBLE);
                                    binding.cartRecycler.setVisibility(View.GONE);
                                } else {
                                    cartAdapter.submitList(new ArrayList<>(cartItems));
                                }
                            });
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiProduct> call, Throwable t) {
                    loadedCount[0]++;
                    if (loadedCount[0] == totalItems) {
                        runOnUiThread(() -> {
                            if (cartItems.isEmpty()) {
                                binding.cartEmptyView.setVisibility(View.VISIBLE);
                                binding.cartRecycler.setVisibility(View.GONE);
                            } else {
                                cartAdapter.submitList(new ArrayList<>(cartItems));
                            }
                        });
                    }
                }
            });
        }
    }


    @Override
    public void onQuantityChanged(CartItem item, int newQuantity) {
        cartRepository.updateQuantity(item.getId(), newQuantity);
        loadCartItems();
    }

    @Override
    public void onRemoveItem(CartItem item) {
        cartRepository.removeItem(item.getId());
        loadCartItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}

