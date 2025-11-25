package com.example.eccomerceapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.OrderRequest;
import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityCheckoutBinding;
import com.example.eccomerceapp.model.CartItem;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_TOTAL = "extra_order_total";

    private ActivityCheckoutBinding binding;
    private OrderRepository orderRepository;
    private CartRepository cartRepository;
    private ApiService apiService;
    private double orderTotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.checkoutToolbar);
        binding.checkoutToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        cartRepository = new CartRepository(this);
        orderRepository = new OrderRepository(this);
        apiService = ApiClient.getInstance();

        orderTotal = getIntent().getDoubleExtra(EXTRA_ORDER_TOTAL, 0d);
        if (orderTotal == 0d) {
            orderTotal = calculateCartTotal();
        }
        binding.checkoutTotal.setText(String.format(Locale.getDefault(), "Rs %.2f", orderTotal));

        binding.buttonPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private double calculateCartTotal() {
        double total = 0d;
        List<CartItem> items = cartRepository.getCartItems();
        for (CartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void placeOrder() {
        String name = getText(binding.inputFullName);
        String phone = getText(binding.inputPhone);
        String address = getText(binding.inputAddress);
        String city = getText(binding.inputCity);

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) ||
                TextUtils.isEmpty(address) || TextUtils.isEmpty(city)) {
            Toast.makeText(this, "Please fill all address fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderTotal <= 0d) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create order request
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.customerName = name;
        orderRequest.phone = phone;
        orderRequest.addressLine = address;
        orderRequest.city = city;
        orderRequest.total = orderTotal;

        // Save locally first
        long localOrderId = orderRepository.placeOrder(name, phone, address, city, orderTotal);
        
        // Also send to server
        apiService.createOrder(orderRequest).enqueue(new Callback<com.example.eccomerceapp.data.api.model.ApiOrder>() {
            @Override
            public void onResponse(Call<com.example.eccomerceapp.data.api.model.ApiOrder> call, Response<com.example.eccomerceapp.data.api.model.ApiOrder> response) {
                if (response.isSuccessful() && localOrderId > 0) {
                    cartRepository.clearCart();
                    Toast.makeText(CheckoutActivity.this, "Order placed!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else if (localOrderId > 0) {
                    // Order saved locally even if server call failed
                    cartRepository.clearCart();
                    Toast.makeText(CheckoutActivity.this, "Order placed! (Saved locally)", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Unable to place order. Try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.eccomerceapp.data.api.model.ApiOrder> call, Throwable t) {
                // If server call fails but local save succeeded, still proceed
                if (localOrderId > 0) {
                    cartRepository.clearCart();
                    Toast.makeText(CheckoutActivity.this, "Order placed! (Saved locally)", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(CheckoutActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

