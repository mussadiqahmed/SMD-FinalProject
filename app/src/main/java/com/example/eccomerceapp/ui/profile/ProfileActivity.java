package com.example.eccomerceapp.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.model.ApiUser;
import com.example.eccomerceapp.data.local.SessionManager;
import com.example.eccomerceapp.data.repository.OrderRepository;
import com.example.eccomerceapp.databinding.ActivityProfileBinding;
import com.example.eccomerceapp.model.Order;
import com.example.eccomerceapp.ui.auth.LoginActivity;
import com.example.eccomerceapp.ui.cart.OrderHistoryActivity;
import com.example.eccomerceapp.ui.catalog.ProductListActivity;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    private SessionManager sessionManager;
    private OrderRepository orderRepository;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.profileToolbar);
        binding.profileToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        sessionManager = new SessionManager(this);
        orderRepository = new OrderRepository(this);
        ApiClient.init(this);
        apiService = ApiClient.getInstance();

        binding.profileAvatar.setImageResource(R.drawable.anim_profile_icon);
        if (binding.profileAvatar.getDrawable() instanceof android.graphics.drawable.Animatable) {
            ((android.graphics.drawable.Animatable) binding.profileAvatar.getDrawable()).start();
        }

        // Load user data from API
        loadUserData();

        binding.buttonProfileOrders.setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));
        binding.buttonProfileFavorites.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_FAVORITES);
            intent.putExtra(ProductListActivity.EXTRA_TITLE, getString(R.string.menu_likes));
            startActivity(intent);
        });
        binding.buttonEditProfile.setOnClickListener(v -> showEditProfileDialog());
        binding.buttonLogout.setOnClickListener(v -> {
            sessionManager.logOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        updateStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        updateStats();
    }

    private void loadUserData() {
        // Show loading state
        binding.profileName.setText("Loading...");
        binding.profileEmail.setText("");

        apiService.getCurrentUser().enqueue(new Callback<ApiUser>() {
            @Override
            public void onResponse(Call<ApiUser> call, Response<ApiUser> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiUser user = response.body();
                    // Update UI with real user data
                    binding.profileName.setText(user.fullName != null ? user.fullName : (user.firstName != null ? user.firstName : "User"));
                    binding.profileEmail.setText(user.email != null ? user.email : getString(R.string.profile_email_placeholder));
                    
                    // Update session manager with latest data
                    if (user.fullName != null) {
                        sessionManager.logIn(user.fullName);
                    }
                    if (user.email != null) {
                        sessionManager.saveUserEmail(user.email);
                    }
                } else {
                    // Fallback to session data if API fails
                    binding.profileName.setText(sessionManager.getUserName());
                    String userEmail = sessionManager.getUserEmail();
                    binding.profileEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : getString(R.string.profile_email_placeholder));
                }
            }

            @Override
            public void onFailure(Call<ApiUser> call, Throwable t) {
                // Fallback to session data on network error
                binding.profileName.setText(sessionManager.getUserName());
                String userEmail = sessionManager.getUserEmail();
                binding.profileEmail.setText(userEmail != null && !userEmail.isEmpty() ? userEmail : getString(R.string.profile_email_placeholder));
            }
        });
    }

    private void updateStats() {
        List<Order> orders = orderRepository.getOrders();
        double total = 0d;
        for (Order order : orders) {
            total += order.getTotalAmount();
        }
        binding.profileOrderCount.setText(String.valueOf(orders.size()));
        binding.profileOrderTotal.setText(String.format(Locale.getDefault(), "Rs %.2f", total));
    }

    private void showEditProfileDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        EditText editName = dialogView.findViewById(R.id.editProfileName);
        EditText editEmail = dialogView.findViewById(R.id.editProfileEmail);
        EditText editCurrentPassword = dialogView.findViewById(R.id.editCurrentPassword);
        EditText editNewPassword = dialogView.findViewById(R.id.editNewPassword);
        EditText editConfirmPassword = dialogView.findViewById(R.id.editConfirmPassword);

        // Pre-fill with current values
        editName.setText(sessionManager.getUserName());
        editEmail.setText(sessionManager.getUserEmail());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Profile")
                .setView(dialogView)
                .setPositiveButton("Save", null) // Set to null to prevent auto-dismiss
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = editName.getText().toString().trim();
                String newEmail = editEmail.getText().toString().trim();
                String currentPassword = editCurrentPassword.getText().toString();
                String newPassword = editNewPassword.getText().toString();
                String confirmPassword = editConfirmPassword.getText().toString();

                if (TextUtils.isEmpty(newName)) {
                    Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(newEmail)) {
                    Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if user wants to change password
                boolean wantsToChangePassword = !TextUtils.isEmpty(currentPassword) || 
                                               !TextUtils.isEmpty(newPassword) || 
                                               !TextUtils.isEmpty(confirmPassword);

                if (wantsToChangePassword) {
                    // All password fields must be filled
                    if (TextUtils.isEmpty(currentPassword)) {
                        editCurrentPassword.setError("Current password is required");
                        return;
                    }
                    if (TextUtils.isEmpty(newPassword)) {
                        editNewPassword.setError("New password is required");
                        return;
                    }
                    if (TextUtils.isEmpty(confirmPassword)) {
                        editConfirmPassword.setError("Please confirm new password");
                        return;
                    }
                    if (!newPassword.equals(confirmPassword)) {
                        editConfirmPassword.setError("Passwords do not match");
                        return;
                    }
                    if (!isValidPassword(newPassword)) {
                        editNewPassword.setError("Password must be 8+ characters with uppercase, lowercase, digit, and special character");
                        return;
                    }

                    // Change password
                    changePassword(currentPassword, newPassword, confirmPassword, newName, newEmail, dialog);
                } else {
                    // Just update profile
                    updateProfile(newName, newEmail, dialog);
                }
            });
        });

        dialog.show();
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

    private void changePassword(String currentPassword, String newPassword, String confirmPassword, 
                                String newName, String newEmail, AlertDialog dialog) {
        com.example.eccomerceapp.data.api.model.ChangePasswordRequest request = 
            new com.example.eccomerceapp.data.api.model.ChangePasswordRequest();
        request.currentPassword = currentPassword;
        request.newPassword = newPassword;
        request.confirmPassword = confirmPassword;

        apiService.changePassword(request).enqueue(new Callback<com.example.eccomerceapp.data.api.model.ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<com.example.eccomerceapp.data.api.model.ChangePasswordResponse> call, 
                                   Response<com.example.eccomerceapp.data.api.model.ChangePasswordResponse> response) {
                if (response.isSuccessful()) {
                    // Password changed successfully, now update profile
                    updateProfile(newName, newEmail, dialog);
                } else {
                    String errorMsg = "Failed to change password";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            // Try to extract message from JSON if possible
                            if (errorMsg.contains("\"message\"")) {
                                int start = errorMsg.indexOf("\"message\"") + 11;
                                int end = errorMsg.indexOf("\"", start);
                                if (end > start) {
                                    errorMsg = errorMsg.substring(start, end);
                                }
                            }
                        } catch (Exception e) {
                            errorMsg = "Failed to change password. Error code: " + response.code();
                        }
                    }
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.example.eccomerceapp.data.api.model.ChangePasswordResponse> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(String newName, String newEmail, AlertDialog dialog) {
        com.example.eccomerceapp.data.api.model.UpdateProfileRequest request = 
            new com.example.eccomerceapp.data.api.model.UpdateProfileRequest();
        request.fullName = newName;
        request.email = newEmail;

        apiService.updateProfile(request).enqueue(new Callback<ApiUser>() {
            @Override
            public void onResponse(Call<ApiUser> call, Response<ApiUser> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiUser updatedUser = response.body();
                    
                    // Update session manager
                    sessionManager.logIn(updatedUser.fullName != null ? updatedUser.fullName : newName);
                    sessionManager.saveUserEmail(updatedUser.email != null ? updatedUser.email : newEmail);

                    // Update UI
                    binding.profileName.setText(updatedUser.fullName != null ? updatedUser.fullName : newName);
                    binding.profileEmail.setText(updatedUser.email != null ? updatedUser.email : newEmail);

                    Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    String errorMsg = "Failed to update profile";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            // Try to extract message from JSON if possible
                            if (errorMsg.contains("\"message\"")) {
                                int start = errorMsg.indexOf("\"message\"") + 11;
                                int end = errorMsg.indexOf("\"", start);
                                if (end > start) {
                                    errorMsg = errorMsg.substring(start, end);
                                }
                            }
                        } catch (Exception e) {
                            errorMsg = "Failed to update profile. Error code: " + response.code();
                        }
                    }
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiUser> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

