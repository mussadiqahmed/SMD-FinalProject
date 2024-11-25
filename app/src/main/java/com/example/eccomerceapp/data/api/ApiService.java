package com.example.eccomerceapp.data.api;

import com.example.eccomerceapp.data.api.model.ApiCategory;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.data.api.model.LoginRequest;
import com.example.eccomerceapp.data.api.model.LoginResponse;
import com.example.eccomerceapp.data.api.model.OrderRequest;
import com.example.eccomerceapp.data.api.model.RegisterRequest;
import com.example.eccomerceapp.data.api.model.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    String BASE_URL = "http://10.0.2.2:8003/api/"; // 10.0.2.2 is Android emulator's alias for localhost

    @GET("categories")
    Call<List<ApiCategory>> getCategories();

    @GET("products")
    Call<List<ApiProduct>> getProducts(
            @Query("categoryId") Long categoryId,
            @Query("categorySlug") String categorySlug,
            @Query("search") String search,
            @Query("featured") Boolean featured
    );

    @GET("products/{id}")
    Call<ApiProduct> getProductById(@retrofit2.http.Path("id") long id);

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/user-login")
    Call<LoginResponse> userLogin(@Body LoginRequest request);

    @POST("orders")
    Call<com.example.eccomerceapp.data.api.model.ApiOrder> createOrder(@Body OrderRequest request);

    @GET("orders")
    Call<List<com.example.eccomerceapp.data.api.model.ApiOrder>> getOrders();

    @GET("orders/customer")
    Call<List<com.example.eccomerceapp.data.api.model.ApiOrder>> getCustomerOrders(
            @Query("customerName") String customerName,
            @Query("phone") String phone
    );

    @GET("auth/me")
    Call<com.example.eccomerceapp.data.api.model.ApiUser> getCurrentUser();

    @POST("auth/change-password")
    Call<com.example.eccomerceapp.data.api.model.ChangePasswordResponse> changePassword(@Body com.example.eccomerceapp.data.api.model.ChangePasswordRequest request);

    @retrofit2.http.PUT("auth/profile")
    Call<com.example.eccomerceapp.data.api.model.ApiUser> updateProfile(@Body com.example.eccomerceapp.data.api.model.UpdateProfileRequest request);
}

