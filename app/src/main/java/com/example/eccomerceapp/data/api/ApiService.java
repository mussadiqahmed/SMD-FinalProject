package com.example.eccomerceapp.data.api;

import com.example.eccomerceapp.data.api.model.ApiCategory;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.data.api.model.LoginRequest;
import com.example.eccomerceapp.data.api.model.LoginResponse;
import com.example.eccomerceapp.data.api.model.RegisterRequest;
import com.example.eccomerceapp.data.api.model.RegisterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    String BASE_URL = "http://10.0.2.2:8001/api/"; // 10.0.2.2 is Android emulator's alias for localhost

    @GET("categories")
    Call<List<ApiCategory>> getCategories();

    @GET("products")
    Call<List<ApiProduct>> getProducts(
            @Query("categoryId") Long categoryId,
            @Query("categorySlug") String categorySlug,
            @Query("search") String search,
            @Query("featured") Boolean featured
    );

    @POST("auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("auth/user-login")
    Call<LoginResponse> userLogin(@Body LoginRequest request);
}

