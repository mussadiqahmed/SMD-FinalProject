package com.example.eccomerceapp.data.repository;

import android.content.Context;

import com.example.eccomerceapp.data.local.AppDatabaseHelper;
import com.example.eccomerceapp.model.Product;

import java.util.List;

public class FavoritesRepository {

    private final AppDatabaseHelper dbHelper;

    public FavoritesRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public boolean addFavorite(long productId) {
        return dbHelper.addFavorite(productId);
    }

    public boolean removeFavorite(long productId) {
        return dbHelper.removeFavorite(productId);
    }

    public boolean isFavorite(long productId) {
        return dbHelper.isFavorite(productId);
    }

    public List<Product> getFavoriteProducts() {
        return dbHelper.getFavoriteProducts();
    }

    public void cleanupOrphanedFavorites() {
        dbHelper.cleanupOrphanedFavorites();
    }
}

