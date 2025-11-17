package com.example.eccomerceapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eccomerceapp.databinding.ActivityCartBinding;
import com.example.eccomerceapp.model.CartItem;
import com.example.eccomerceapp.data.repository.CartRepository;

import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity implements CartAdapter.CartActionListener {

    private ActivityCartBinding binding;
    private CartRepository cartRepository;
    private CartAdapter cartAdapter;
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
        List<CartItem> items = cartRepository.getCartItems();
        cartAdapter.submitList(items);
        binding.cartEmptyView.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        binding.cartRecycler.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
        currentTotal = 0;
        for (CartItem item : items) {
            currentTotal += item.getTotalPrice();
        }
        binding.cartTotalPrice.setText(String.format(Locale.getDefault(), "$%.2f", currentTotal));
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

