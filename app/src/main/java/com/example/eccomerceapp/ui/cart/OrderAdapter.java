package com.example.eccomerceapp.ui.cart;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eccomerceapp.databinding.ItemOrderBinding;
import com.example.eccomerceapp.model.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final List<Order> orders = new ArrayList<>();
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public void submitList(List<Order> items) {
        orders.clear();
        if (items != null) {
            orders.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        holder.bind(orders.get(position));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {

        private final ItemOrderBinding binding;

        OrderViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Order order) {
            binding.orderId.setText(String.format(Locale.getDefault(), "#%05d", order.getId()));
            binding.orderStatus.setText(order.getStatus());
            binding.orderDate.setText(dateFormat.format(new Date(order.getCreatedAt())));
            binding.orderTotal.setText(String.format(Locale.getDefault(), "Rs %.2f", order.getTotalAmount()));
            binding.orderAddress.setText(String.format(
                    Locale.getDefault(), "%s, %s", order.getAddressLine(), order.getCity()));
        }
    }
}

