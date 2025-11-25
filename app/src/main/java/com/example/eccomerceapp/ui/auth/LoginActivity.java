package com.example.eccomerceapp.ui.auth;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.LoginRequest;
import com.example.eccomerceapp.data.api.model.LoginResponse;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.databinding.ActivityLoginBinding;
import com.example.eccomerceapp.ui.common.ToastHelper;
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
        ApiClient.init(this);
        apiService = ApiClient.getInstance();

        setupSignupPrompt();
        binding.buttonLogin.setOnClickListener(v -> attemptLogin());
        binding.forgotPassword.setOnClickListener(v -> openForgotPasswordEmail());
    }

    private void attemptLogin() {
        String email = binding.inputUsername.getText() != null ? binding.inputUsername.getText().toString().trim() : "";
        String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            ToastHelper.showToastWithLogo(this, "Please enter email and password");
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
                        String userName = loginResponse.user.fullName != null ? loginResponse.user.fullName : (loginResponse.user.firstName != null ? loginResponse.user.firstName : loginResponse.user.email);
                        sessionManager.logIn(userName);
                        sessionManager.saveToken(loginResponse.token);
                        sessionManager.saveUserEmail(loginResponse.user.email);
                        ToastHelper.showToastWithLogo(LoginActivity.this, "Login successful!");
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
                    ToastHelper.showToastWithLogo(LoginActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                binding.buttonLogin.setEnabled(true);
                binding.buttonLogin.setText("Login");
                ToastHelper.showToastWithLogo(LoginActivity.this, "Network error: " + t.getMessage());
            }
        });
    }

    private void setupSignupPrompt() {
        String fullText = getString(R.string.label_signup_prompt);
        String clickableText = "Sign Up";
        
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(clickableText);
        int endIndex = startIndex + clickableText.length();
        
        if (startIndex >= 0) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(LoginActivity.this, SignupActivity.class));
                }
                
                @Override
                public void updateDrawState(TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setColor(getColor(R.color.primary_orange));
                    ds.setUnderlineText(false);
                }
            };
            
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        binding.signupPrompt.setText(spannableString);
        binding.signupPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        binding.signupPrompt.setHighlightColor(Color.TRANSPARENT);
    }

    private void openForgotPasswordEmail() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"nova@support.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Password Reset Request");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please help me reset my password.");

        PackageManager packageManager = getPackageManager();
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } else {
            ToastHelper.showToastWithLogo(this, "No email app found. Please contact nova@support.com");
        }
    }
}

