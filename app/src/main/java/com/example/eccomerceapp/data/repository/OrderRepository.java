package com.example.eccomerceapp.data.repository;

import android.content.Context;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.local.AppDatabaseHelper;
import com.example.eccomerceapp.model.Order;

import java.util.List;

public class OrderRepository {

    private final AppDatabaseHelper dbHelper;
    private final Context appContext;

    public OrderRepository(Context context) {
        this.appContext = context.getApplicationContext();
        dbHelper = new AppDatabaseHelper(context);
    }

    public long placeOrder(String customerName,
                           String phone,
                           String addressLine,
                           String city,
                           double total) {
        long timestamp = System.currentTimeMillis();
        String status = appContext.getString(R.string.order_status_processing);
        return dbHelper.insertOrder(customerName, phone, addressLine, city, total, status, timestamp);
    }

    public List<Order> getOrders() {
        return dbHelper.getOrders();
    }
}

