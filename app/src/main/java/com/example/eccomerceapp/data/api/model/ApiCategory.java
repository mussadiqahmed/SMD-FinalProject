package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;

public class ApiCategory {
    @SerializedName("id")
    public Long id;

    @SerializedName("title")
    public String title;

    @SerializedName("slug")
    public String slug;

    @SerializedName("imageUrl")
    public String imageUrl;
}

