package com.example.eccomerceapp.ui.cart;

import android.view.LayoutInflater;
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

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final ItemCartBinding binding;

        CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CartItem item) {
            binding.cartProductName.setText(item.getProduct().getName());
            binding.cartProductPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getProduct().getPrice()));
            binding.cartProductSize.setText(item.getSelectedSize() == null ? "Size: -" : "Size: " + item.getSelectedSize());
            binding.cartProductColor.setText(item.getSelectedColor() == null ? "Color: -" : "Color: " + item.getSelectedColor());
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