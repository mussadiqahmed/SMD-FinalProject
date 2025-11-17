package com.example.eccomerceapp.data.repository;

import android.content.Context;

import com.example.eccomerceapp.data.local.AppDatabaseHelper;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Product;

import java.util.List;

public class ProductRepository {

    private final AppDatabaseHelper dbHelper;

    public ProductRepository(Context context) {
        this.dbHelper = new AppDatabaseHelper(context);
    }

    public List<Category> loadCategories() {
        return dbHelper.getCategories();
    }

    public List<Product> loadProducts(Long categoryId) {
        return dbHelper.getProducts(categoryId);
    }

    public List<Product> searchProducts(String keyword) {
        return dbHelper.searchProducts(keyword);
    }

    public Product findProduct(long id) {
        return dbHelper.getProductById(id);
    }
}

