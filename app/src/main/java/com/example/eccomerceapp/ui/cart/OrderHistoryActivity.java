package com.example.eccomerceapp.ui.cart;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityOrderHistoryBinding;
import com.example.eccomerceapp.model.Order;

import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private ActivityOrderHistoryBinding binding;
    private OrderRepository orderRepository;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.ordersToolbar);
        binding.ordersToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        orderRepository = new OrderRepository(this);
        orderAdapter = new OrderAdapter();

        binding.ordersRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.ordersRecycler.setAdapter(orderAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = orderRepository.getOrders();
        orderAdapter.submitList(orders);
        boolean isEmpty = orders.isEmpty();
        binding.ordersEmptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.ordersRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}

