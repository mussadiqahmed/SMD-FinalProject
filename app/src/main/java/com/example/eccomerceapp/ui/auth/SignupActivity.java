package com.example.eccomerceapp.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.RegisterRequest;
import com.example.eccomerceapp.data.api.model.RegisterResponse;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.databinding.ActivitySignupBinding;
import com.example.eccomerceapp.ui.common.ToastHelper;
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

        setupGenderDropdown();
        setupLoginPrompt();
        setupPasswordValidation();
        binding.buttonSignup.setOnClickListener(v -> attemptSignup());
    }

    private void setupGenderDropdown() {
        String[] genders = getResources().getStringArray(R.array.gender_options);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, genders);
        binding.inputGender.setAdapter(adapter);
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) {
                hasSpecial = true;
            }
        }
        
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    private void setupLoginPrompt() {
        String fullText = getString(R.string.label_login_prompt);
        String clickableText = "Login";
        
        SpannableString spannableString = new SpannableString(fullText);
        int startIndex = fullText.indexOf(clickableText);
        int endIndex = startIndex + clickableText.length();
        
        if (startIndex >= 0) {
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    finish();
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
        
        binding.loginPrompt.setText(spannableString);
        binding.loginPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        binding.loginPrompt.setHighlightColor(Color.TRANSPARENT);
    }

    private void setupPasswordValidation() {
        binding.inputPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordValidation(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updatePasswordValidation(String password) {
        if (password == null || password.isEmpty()) {
            binding.passwordValidationContainer.setVisibility(View.GONE);
            return;
        }

        binding.passwordValidationContainer.setVisibility(View.VISIBLE);

        // Check each requirement
        boolean hasLength = password.length() >= 8;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        // Update validation text views with checkmarks/X marks and colors
        updateValidationText(binding.validationLength, hasLength, "At least 8 characters");
        updateValidationText(binding.validationUppercase, hasUpper, "One uppercase letter");
        updateValidationText(binding.validationLowercase, hasLower, "One lowercase letter");
        updateValidationText(binding.validationDigit, hasDigit, "One digit");
        updateValidationText(binding.validationSpecial, hasSpecial, "One special character");
    }

    private void updateValidationText(android.widget.TextView textView, boolean isValid, String requirement) {
        String prefix = isValid ? "✓ " : "✗ ";
        int color = isValid ? getColor(R.color.primary_orange) : getColor(R.color.accent_gray);
        textView.setText(prefix + requirement);
        textView.setTextColor(color);
    }

    private void attemptSignup() {
        String firstName = binding.inputFirstName.getText() != null ? binding.inputFirstName.getText().toString().trim() : "";
        String lastName = binding.inputLastName.getText() != null ? binding.inputLastName.getText().toString().trim() : "";
        String gender = binding.inputGender.getText() != null ? binding.inputGender.getText().toString().trim() : "";
        String email = binding.inputEmail.getText() != null ? binding.inputEmail.getText().toString().trim() : "";
        String password = binding.inputPassword.getText() != null ? binding.inputPassword.getText().toString() : "";
        String confirmPassword = binding.inputConfirmPassword.getText() != null ? binding.inputConfirmPassword.getText().toString() : "";

        // Reset errors
        binding.firstNameLayout.setError(null);
        binding.lastNameLayout.setError(null);
        binding.genderLayout.setError(null);
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
        binding.confirmPasswordLayout.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(firstName)) {
            binding.firstNameLayout.setError("First name is required");
            focusView = binding.inputFirstName;
            cancel = true;
        } else if (TextUtils.isEmpty(lastName)) {
            binding.lastNameLayout.setError("Last name is required");
            focusView = binding.inputLastName;
            cancel = true;
        } else if (TextUtils.isEmpty(email)) {
            binding.emailLayout.setError("Email is required");
            focusView = binding.inputEmail;
            cancel = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.setError("Invalid email address");
            focusView = binding.inputEmail;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            binding.passwordLayout.setError("Password is required");
            focusView = binding.inputPassword;
            cancel = true;
        } else if (!isValidPassword(password)) {
            binding.passwordLayout.setError("Password must be 8+ characters with uppercase, lowercase, digit, and special character");
            focusView = binding.inputPassword;
            cancel = true;
        } else if (TextUtils.isEmpty(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Confirm password is required");
            focusView = binding.inputConfirmPassword;
            cancel = true;
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordLayout.setError("Passwords do not match");
            focusView = binding.inputConfirmPassword;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        binding.buttonSignup.setEnabled(false);
        binding.buttonSignup.setText("Signing up...");

        RegisterRequest request = new RegisterRequest();
        request.firstName = firstName;
        request.lastName = lastName;
        request.gender = gender.isEmpty() ? null : gender;
        request.email = email;
        request.password = password;
        request.confirmPassword = confirmPassword;

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                binding.buttonSignup.setEnabled(true);
                binding.buttonSignup.setText(getString(R.string.action_signup));

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();
                    if (registerResponse.token != null && registerResponse.user != null) {
                        String userName = registerResponse.user.firstName != null ? registerResponse.user.firstName : registerResponse.user.email;
                        sessionManager.logIn(userName);
                        sessionManager.saveToken(registerResponse.token);
                        sessionManager.saveUserEmail(registerResponse.user.email);
                        ToastHelper.showToastWithLogo(SignupActivity.this, "Account created successfully!");
                        Intent intent = new Intent(SignupActivity.this, HomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        ToastHelper.showToastWithLogo(SignupActivity.this, "Registration failed: Invalid response");
                    }
                } else {
                    String errorMsg = "Registration failed";
                    try {
                        if (response.errorBody() != null) {
                            // Try to parse error response
                            errorMsg = response.message();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ToastHelper.showToastWithLogo(SignupActivity.this, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                binding.buttonSignup.setEnabled(true);
                binding.buttonSignup.setText(getString(R.string.action_signup));
                String errorMessage = "Network error: " + t.getMessage();
                if (t.getMessage() != null && t.getMessage().contains("CLEARTEXT")) {
                    errorMessage = "Network error: Please check your connection settings";
                }
                ToastHelper.showToastWithLogo(SignupActivity.this, errorMessage);
            }
        });
    }
}

