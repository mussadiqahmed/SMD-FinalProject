package com.example.eccomerceapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.LoginRequest;
import com.example.eccomerceapp.data.api.model.LoginResponse;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.databinding.ActivityLoginBinding;
import com.example.eccomerceapp.ui.home.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getInstance();

        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.signupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, SignupActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.inputUsername.getText() != null ? binding.inputUsername.getText().toString().trim() : "";
        String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonLogin.setEnabled(false);
        binding.buttonLogin.setText("Logging in...");

        LoginRequest request = new LoginRequest();
        request.email = email;
        request.password = password;

        apiService.userLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                binding.buttonLogin.setEnabled(true);
                binding.buttonLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    if (loginResponse.token != null) {
                        sessionManager.logIn(loginResponse.user.email);
                        sessionManager.saveToken(loginResponse.token);
                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    String errorMsg = "Invalid email or password";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.buttonLogin.setEnabled(true);
                binding.buttonLogin.setText("Login");
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

