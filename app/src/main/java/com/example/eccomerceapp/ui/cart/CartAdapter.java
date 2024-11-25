package com.example.eccomerceapp.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eccomerceapp.databinding.ItemCartBinding;
import com.example.eccomerceapp.model.CartItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartActionListener {
        void onQuantityChanged(CartItem item, int newQuantity);

        void onRemoveItem(CartItem item);
    }

    private final CartActionListener listener;
    private final List<CartItem> cartItems = new ArrayList<>();

    public CartAdapter(CartActionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> items) {
        cartItems.clear();
        if (items != null) {
            cartItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    /**
     * Cleans a value string by removing JSON array brackets and quotes
     */
    private String cleanValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String cleaned = value.trim();
        
        // Remove JSON array brackets if present
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }
        
        // Remove quotes if present (handle multiple quotes)
        cleaned = cleaned.replaceAll("^\"+|\"+$", "");
        
        // Remove any remaining whitespace
        cleaned = cleaned.trim();
        
        // If it's still empty or just brackets/quotes, return null
        if (cleaned.isEmpty() || cleaned.equals("null") || cleaned.equals("[]")) {
            return null;
        }
        
        return cleaned;
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.cartProductName.setText(item.getProduct().getName());
            binding.cartProductPrice.setText(String.format(Locale.getDefault(), "Rs %.2f", item.getProduct().getPrice()));
            
            // Clean size string to remove JSON brackets and quotes
            String cleanSize = cleanValue(item.getSelectedSize());
            if (cleanSize != null && !cleanSize.isEmpty()) {
                binding.cartProductSize.setVisibility(View.VISIBLE);
                binding.cartProductSize.setText("Size: " + cleanSize);
            } else {
                binding.cartProductSize.setVisibility(View.GONE);
            }
            
            // Clean color string to remove JSON brackets and quotes
            String cleanColor = cleanValue(item.getSelectedColor());
            if (cleanColor != null && !cleanColor.isEmpty()) {
                binding.cartProductColor.setVisibility(View.VISIBLE);
                binding.cartProductColor.setText("Color: " + cleanColor);
            } else {
                binding.cartProductColor.setVisibility(View.GONE);
            }
            
            binding.quantityValue.setText(String.valueOf(item.getQuantity()));

            Glide.with(binding.getRoot().getContext())
                    .load(item.getProduct().getImageUrl())
                    .centerCrop()
                    .into(binding.cartProductImage);

            binding.buttonIncrease.setOnClickListener(v -> {
                int newQuantity = item.getQuantity() + 1;
                if (listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            binding.buttonDecrease.setOnClickListener(v -> {
                int newQuantity = Math.max(1, item.getQuantity() - 1);
                if (newQuantity != item.getQuantity() && listener != null) {
                    listener.onQuantityChanged(item, newQuantity);
                }
            });

            binding.removeButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRemoveItem(item);
                }
            });
        }
    }
}