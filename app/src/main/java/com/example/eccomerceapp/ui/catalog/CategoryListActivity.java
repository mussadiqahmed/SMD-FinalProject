package com.example.eccomerceapp.ui.catalog;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.eccomerceapp.data.repository.ProductRepository;
import com.example.eccomerceapp.databinding.ActivityCategoryListBinding;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.ui.home.CategoryAdapter;

import java.util.List;

public class CategoryListActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener {

    private ActivityCategoryListBinding binding;
    private ProductRepository productRepository;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.categoryToolbar);
        binding.categoryToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        productRepository = new ProductRepository(this);
        categoryAdapter = new CategoryAdapter(this);

        binding.categoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.categoryRecycler.setAdapter(categoryAdapter);

        loadCategories();
    }

    private void loadCategories() {
        List<Category> categories = productRepository.loadCategories();
        categoryAdapter.submitList(categories);
    }

    @Override
    public void onCategorySelected(Category category) {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra(ProductListActivity.EXTRA_MODE, ProductListActivity.MODE_CATEGORY);
        intent.putExtra(ProductListActivity.EXTRA_CATEGORY_ID, category.getId());
        intent.putExtra(ProductListActivity.EXTRA_TITLE, category.getTitle());
        startActivity(intent);
    }
}

