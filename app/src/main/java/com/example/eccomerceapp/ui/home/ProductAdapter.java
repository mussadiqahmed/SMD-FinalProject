package com.example.eccomerceapp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eccomerceapp.R;
import com.example.eccomerceapp.databinding.ItemProductBinding;
import com.example.eccomerceapp.data.repository.FavoritesRepository;
import com.example.eccomerceapp.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClicked(Product product);
    }

    private final OnProductClickListener listener;
    private final List<Product> products = new ArrayList<>();
    private FavoritesRepository favoritesRepository;

    public ProductAdapter(OnProductClickListener listener, Context context) {
        this.listener = listener;
        this.favoritesRepository = new FavoritesRepository(context);
    }

    public void submitList(List<Product> items) {
        products.clear();
        if (items != null) {
            products.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(products.get(position));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {

        private final ItemProductBinding binding;

        ProductViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Product product) {
            binding.productName.setText(product.getName());
            
            // Handle price display with discount
            if (product.hasDiscount()) {
                // Show original price (crossed out) and discounted price
                double originalPrice = product.getPrice();
                double discountedPrice = product.getDiscountedPrice();
                
                binding.productOriginalPrice.setVisibility(android.view.View.VISIBLE);
                binding.productOriginalPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", originalPrice));
                binding.productOriginalPrice.setPaintFlags(
                    binding.productOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                );
                
                binding.productPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", discountedPrice));
            } else {
                // No discount, show only the price
                binding.productOriginalPrice.setVisibility(android.view.View.GONE);
                binding.productPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", product.getPrice()));
            }
            
            // Load image with error handling
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(binding.getRoot().getContext())
                        .load(imageUrl)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(binding.productImage);
            } else {
                // Set placeholder if no image URL
                binding.productImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }

            // Check if product is favorited and set button state and icon
            boolean isFavorite = favoritesRepository.isFavorite(product.getId());
            binding.favoriteButton.setSelected(isFavorite);
            
            // Set icon based on favorite state: filled when liked, outline when not liked
            if (isFavorite) {
                binding.favoriteButton.setImageResource(R.drawable.ic_heart);
                binding.favoriteButton.setColorFilter(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary_orange));
            } else {
                binding.favoriteButton.setImageResource(R.drawable.ic_heart_outline);
                binding.favoriteButton.setColorFilter(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary_orange));
            }

            // Handle favorite button click
            binding.favoriteButton.setOnClickListener(v -> {
                boolean newFavoriteState = !binding.favoriteButton.isSelected();
                binding.favoriteButton.setSelected(newFavoriteState);
                
                // Update icon based on new state
                if (newFavoriteState) {
                    binding.favoriteButton.setImageResource(R.drawable.ic_heart);
                    favoritesRepository.addFavorite(product.getId());
                } else {
                    binding.favoriteButton.setImageResource(R.drawable.ic_heart_outline);
                    favoritesRepository.removeFavorite(product.getId());
                }
                binding.favoriteButton.setColorFilter(ContextCompat.getColor(binding.getRoot().getContext(), R.color.primary_orange));
            });

            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClicked(product);
                }
            });
        }
    }
}

