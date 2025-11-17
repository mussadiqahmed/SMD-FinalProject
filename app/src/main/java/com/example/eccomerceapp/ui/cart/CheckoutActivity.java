package com.example.eccomerceapp.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.data.repository.CartRepository;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityCheckoutBinding;
import com.example.eccomerceapp.model.CartItem;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_ORDER_TOTAL = "extra_order_total";

    private ActivityCheckoutBinding binding;
    private OrderRepository orderRepository;
    private CartRepository cartRepository;
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

        orderTotal = getIntent().getDoubleExtra(EXTRA_ORDER_TOTAL, 0d);
        if (orderTotal == 0d) {
            orderTotal = calculateCartTotal();
        }
        binding.checkoutTotal.setText(String.format(Locale.getDefault(), "$%.2f", orderTotal));

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

        long orderId = orderRepository.placeOrder(name, phone, address, city, orderTotal);
        if (orderId > 0) {
            cartRepository.clearCart();
            Toast.makeText(this, "Order placed!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, OrderHistoryActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Unable to place order. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}

