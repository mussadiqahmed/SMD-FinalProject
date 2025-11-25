package com.example.eccomerceapp.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ecommerce_prefs";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN = "auth_token";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void logIn(String userName) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, userName)
                .apply();
    }

    public void logOut() {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_NAME)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "Guest");
    }

    public void saveToken(String token) {
        preferences.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return preferences.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        preferences.edit().remove(KEY_TOKEN).apply();
    }
    
    public void saveUserEmail(String email) {
        preferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }
    
    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }
}

