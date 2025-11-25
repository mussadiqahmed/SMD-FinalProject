package com.example.eccomerceapp.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eccomerceapp.databinding.ItemCategoryBinding;
import com.example.eccomerceapp.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    public interface OnCategoryClickListener {
        void onCategorySelected(Category category);
    }

    private final OnCategoryClickListener listener;
    private final List<Category> categories = new ArrayList<>();

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Category> items) {
        categories.clear();
        if (items != null) {
            categories.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CategoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.bind(categories.get(position));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemCategoryBinding binding;

        CategoryViewHolder(@NonNull ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.categoryTitle.setText(category.getTitle());

            View root = binding.getRoot();
            root.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategorySelected(category);
                }
            });
        }
    }
}

