package com.example.eccomerceapp.data.repository;

import android.content.Context;

import com.example.eccomerceapp.R;
import com.example.eccomerceapp.data.api.ApiService;
import com.example.eccomerceapp.data.api.ApiClient;
import com.example.eccomerceapp.data.api.model.ApiOrder;
import com.example.eccomerceapp.data.local.AppDatabaseHelper;
import com.example.eccomerceapp.model.Order;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderRepository {

    private final AppDatabaseHelper dbHelper;
    private final Context appContext;
    private final ApiService apiService;

    public OrderRepository(Context context) {
        this.appContext = context.getApplicationContext();
        dbHelper = new AppDatabaseHelper(context);
        apiService = ApiClient.getInstance();
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

    public void clearAllOrders() {
        dbHelper.clearAllOrders();
    }

    public void syncOrdersFromServer(Runnable onComplete) {
        // Get customer info from most recent order
        List<Order> localOrders = dbHelper.getOrders();
        if (localOrders.isEmpty()) {
            // No local orders, can't sync
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        // Use customer info from most recent order
        Order mostRecentOrder = localOrders.get(0);
        String customerName = mostRecentOrder.getCustomerName();
        String phone = mostRecentOrder.getPhone();

        // Fetch orders from server for this customer
        apiService.getCustomerOrders(customerName, phone).enqueue(new Callback<List<ApiOrder>>() {
            @Override
            public void onResponse(Call<List<ApiOrder>> call, Response<List<ApiOrder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Sync each order to local database
                    for (ApiOrder apiOrder : response.body()) {
                        if (apiOrder.id != null && apiOrder.customerName != null && apiOrder.total != null && apiOrder.createdAt != null) {
                            dbHelper.updateOrInsertOrder(
                                apiOrder.id,
                                apiOrder.customerName,
                                apiOrder.phone != null ? apiOrder.phone : "",
                                apiOrder.addressLine != null ? apiOrder.addressLine : "",
                                apiOrder.city != null ? apiOrder.city : "",
                                apiOrder.total,
                                apiOrder.status != null ? apiOrder.status : "processing",
                                apiOrder.createdAt * 1000 // Convert from seconds to milliseconds
                            );
                        }
                    }
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<List<ApiOrder>> call, Throwable t) {
                // If API call fails, just use local data
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }
}

