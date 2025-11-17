package com.example.eccomerceapp.ui.wallet;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityWalletBinding;
import com.example.eccomerceapp.model.Order;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class WalletActivity extends AppCompatActivity {

    private ActivityWalletBinding binding;
    private OrderRepository orderRepository;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.walletToolbar);
        binding.walletToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        orderRepository = new OrderRepository(this);

        binding.buttonViewOrders.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        loadWalletStats();
    }

    private void loadWalletStats() {
        List<Order> orders = orderRepository.getOrders();
        double totalSpent = 0d;
        for (Order order : orders) {
            totalSpent += order.getTotalAmount();
        }
        int orderCount = orders.size();
        double average = orderCount == 0 ? 0 : totalSpent / orderCount;

        binding.walletTotalSpent.setText(String.format(Locale.getDefault(), "$%.2f", totalSpent));
        binding.walletOrdersCount.setText(String.valueOf(orderCount));
        binding.walletAverage.setText(String.format(Locale.getDefault(), "$%.2f", average));

        if (orderCount > 0) {
            Order latest = orders.get(0);
            String lastInfo = latest.getStatus() + " • " + dateFormat.format(latest.getCreatedAt());
            binding.walletLastOrder.setText(lastInfo);
        } else {
            binding.walletLastOrder.setText(getString(R.string.wallet_no_orders));
        }
    }
}

