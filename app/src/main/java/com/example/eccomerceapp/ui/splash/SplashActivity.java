package com.example.eccomerceapp.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.databinding.ActivitySplashBinding;
import com.example.eccomerceapp.ui.auth.LoginActivity;
import com.example.eccomerceapp.ui.home.HomeActivity;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sessionManager = new SessionManager(this);
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, sessionManager.isLoggedIn() ? HomeActivity.class : LoginActivity.class);
            startActivity(intent);
            finish();
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}

