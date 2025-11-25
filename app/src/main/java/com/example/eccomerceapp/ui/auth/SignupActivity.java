package com.example.eccomerceapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.RegisterRequest;
import com.example.eccomerceapp.data.api.model.RegisterResponse;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.databinding.ActivitySignupBinding;
import com.example.eccomerceapp.ui.home.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getInstance();

        binding.buttonSignup.setOnClickListener(v -> attemptSignup());
        binding.loginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String firstName = binding.inputFirstName.getText() != null ? binding.inputFirstName.getText().toString().trim() : "";
        String lastName = binding.inputLastName.getText() != null ? binding.inputLastName.getText().toString().trim() : "";
        String gender = binding.inputGender.getText() != null ? binding.inputGender.getText().toString().trim() : "";
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";
        String confirmPassword = binding.inputConfirmPassword.getText() != null ? binding.inputConfirmPassword.getText().toString() : "";

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.buttonSignup.setEnabled(false);
        binding.buttonSignup.setText("Signing up...");

        RegisterRequest request = new RegisterRequest();
        request.firstName = firstName;
        request.lastName = lastName;
        request.gender = gender;
        request.email = email;
        request.password = password;
        request.confirmPassword = confirmPassword;

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                binding.buttonSignup.setEnabled(true);
                binding.buttonSignup.setText("Sign Up");

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.token != null) {
                        sessionManager.logIn(registerResponse.user.email);
                        sessionManager.saveToken(registerResponse.token);
                        Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    String errorMsg = "Registration failed";
                    if (response.body() != null && response.body().message != null) {
                        errorMsg = response.body().message;
                    }
                    Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                binding.buttonSignup.setEnabled(true);
                binding.buttonSignup.setText("Sign Up");
                Toast.makeText(SignupActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

