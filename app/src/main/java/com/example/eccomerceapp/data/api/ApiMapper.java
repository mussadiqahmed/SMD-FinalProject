package com.example.eccomerceapp.data.api;

import com.example.eccomerceapp.data.api.model.ApiCategory;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Product;

import java.util.ArrayList;
import java.util.List;

public class ApiMapper {
    public static Category toCategory(ApiCategory apiCategory) {
        return new Category(
                apiCategory.id,
                apiCategory.title,
                apiCategory.imageUrl != null ? apiCategory.imageUrl : ""
        );
    }

    public static Product toProduct(ApiProduct apiProduct) {
        String imageUrl = apiProduct.imageUrl;
        if (imageUrl == null && apiProduct.images != null && !apiProduct.images.isEmpty()) {
            imageUrl = apiProduct.images.get(0);
        }
        if (imageUrl == null) {
            imageUrl = "";
        }

        String sizesCsv = "";
        if (apiProduct.sizes != null && !apiProduct.sizes.isEmpty()) {
            sizesCsv = String.join(",", apiProduct.sizes);
        }

        String colorsCsv = "";
        if (apiProduct.colors != null && !apiProduct.colors.isEmpty()) {
            colorsCsv = String.join(",", apiProduct.colors);
        }

        return new Product(
                apiProduct.id,
                apiProduct.name,
                apiProduct.description != null ? apiProduct.description : "",
                apiProduct.price != null ? apiProduct.price : 0.0,
                imageUrl,
                apiProduct.categoryId != null ? apiProduct.categoryId : 0,
                sizesCsv,
                colorsCsv
        );
    }

    public static List<Category> toCategoryList(List<ApiCategory> apiCategories) {
        List<Category> categories = new ArrayList<>();
        if (apiCategories != null) {
            for (ApiCategory apiCategory : apiCategories) {
                categories.add(toCategory(apiCategory));
            }
        }
        return categories;
    }

    public static List<Product> toProductList(List<ApiProduct> apiProducts) {
        List<Product> products = new ArrayList<>();
        if (apiProducts != null) {
            for (ApiProduct apiProduct : apiProducts) {
                products.add(toProduct(apiProduct));
            }
        }
        return products;
    }
}

