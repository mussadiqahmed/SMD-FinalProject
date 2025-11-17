package com.example.eccomerceapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityProfileBinding;
import com.example.eccomerceapp.model.Order;
import com.example.eccomerceapp.ui.auth.LoginActivity;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;
import com.example.eccomerceapp.ui.catalog.ProductListActivity;
import com.example.eccomerceapp.ui.wallet.WalletActivity;

import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.profileToolbar);
        binding.profileToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        sessionManager = new SessionManager(this);
        orderRepository = new OrderRepository(this);

        binding.profileAvatar.setImageResource(R.drawable.anim_profile_icon);
        if (binding.profileAvatar.getDrawable() instanceof android.graphics.drawable.Animatable) {
            ((android.graphics.drawable.Animatable) binding.profileAvatar.getDrawable()).start();
        }

        binding.profileName.setText(sessionManager.getUserName());
        binding.profileEmail.setText(getString(R.string.profile_email_placeholder));

        binding.buttonProfileOrders.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        binding.buttonProfileFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_FAVORITES);
            intent.putExtra(ProductListActivity.EXTRA_TITLE, getString(R.string.menu_likes));
            startActivity(intent);
        });
        binding.buttonProfileWallet.setOnClickListener(v ->
                startActivity(new Intent(this, WalletActivity.class)));
        binding.buttonLogout.setOnClickListener(v -> {
            sessionManager.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        updateStats();
    }

    private void updateStats() {
        List<Order> orders = orderRepository.getOrders();
        double total = 0d;
        for (Order order : orders) {
            total += order.getTotalAmount();
        }
        binding.profileOrderCount.setText(String.valueOf(orders.size()));
        binding.profileOrderTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
    }
}

