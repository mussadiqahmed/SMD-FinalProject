package com.example.eccomerceapp.data.repository;

import android.content.Context;

import com.example.eccomerceapp.data.local.AppDatabaseHelper;
import com.example.eccomerceapp.model.CartItem;

import java.util.List;

public class CartRepository {

    private final AppDatabaseHelper dbHelper;

    public CartRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public long addToCart(long productId, int quantity, String size, String color) {
        return dbHelper.addToCart(productId, quantity, size, color);
    }

    public boolean updateQuantity(long cartId, int quantity) {
        return dbHelper.updateCartQuantity(cartId, quantity);
    }

    public boolean removeItem(long cartId) {
        return dbHelper.removeCartItem(cartId);
    }

    public void clearCart() {
        dbHelper.clearCart();
    }

    public List<CartItem> getCartItems() {
        return dbHelper.getCartItems();
    }

    public int getCartCount() {
        return dbHelper.getCartItemCount();
    }
}

