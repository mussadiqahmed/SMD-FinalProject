package com.example.eccomerceapp.data.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiProduct {
    @SerializedName("id")
    public Long id;

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("price")
    public Double price;

    @SerializedName("discountPercent")
    public Double discountPercent;

    @SerializedName("imageUrl")
    public String imageUrl;

    @SerializedName("images")
    public List<String> images;

    @SerializedName("categoryId")
    public Long categoryId;

    @SerializedName("category")
    public CategoryInfo category;

    @SerializedName("sizes")
    public List<String> sizes;

    @SerializedName("colors")
    public List<String> colors;

    @SerializedName("stock")
    public Integer stock;

    @SerializedName("featured")
    public Boolean featured;

    public static class CategoryInfo {
        @SerializedName("id")
        public Long id;

        @SerializedName("title")
        public String title;

        @SerializedName("slug")
        public String slug;
    }
}

