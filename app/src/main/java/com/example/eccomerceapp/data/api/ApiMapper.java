package com.example.eccomerceapp.data.api;

import com.example.eccomerceapp.data.api.model.ApiCategory;
import com.example.eccomerceapp.data.api.model.ApiProduct;
import com.example.eccomerceapp.model.Category;
import com.example.eccomerceapp.model.Product;

import java.util.ArrayList;
import java.util.Collections;
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
        
        // Convert relative URLs to full URLs for stored images
        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (imageUrl.startsWith("/uploads/") || imageUrl.startsWith("uploads/")) {
                String baseUrl = ApiService.BASE_URL.replace("/api/", "");
                if (imageUrl.startsWith("/")) {
                    imageUrl = baseUrl + imageUrl;
                } else {
                    imageUrl = baseUrl + "/" + imageUrl;
                }
            }
            
            // Convert localhost URLs to Android emulator address
            if (imageUrl.contains("localhost")) {
                imageUrl = imageUrl.replace("localhost", "10.0.2.2");
            }
            // Also handle 127.0.0.1
            if (imageUrl.contains("127.0.0.1")) {
                imageUrl = imageUrl.replace("127.0.0.1", "10.0.2.2");
            }
            // Fix port if needed (convert 8001 to 8003)
            if (imageUrl.contains(":8001")) {
                imageUrl = imageUrl.replace(":8001", ":8003");
            }
        }

        // Convert sizes array to CSV string, handling any JSON string format
        String sizesCsv = "";
        if (apiProduct.sizes != null && !apiProduct.sizes.isEmpty()) {
            List<String> cleanSizes = new ArrayList<>();
            for (String size : apiProduct.sizes) {
                if (size != null) {
                    // Remove any JSON array brackets or quotes
                    String cleanSize = size.trim()
                        .replaceAll("^\\[\"", "")
                        .replaceAll("\"\\]$", "")
                        .replaceAll("^\"", "")
                        .replaceAll("\"$", "")
                        .replaceAll("^\\[", "")
                        .replaceAll("\\]$", "");
                    if (!cleanSize.isEmpty()) {
                        cleanSizes.add(cleanSize);
                    }
                }
            }
            sizesCsv = String.join(",", cleanSizes);
        }

        // Convert colors array to CSV string, handling any JSON string format
        String colorsCsv = "";
        if (apiProduct.colors != null && !apiProduct.colors.isEmpty()) {
            List<String> cleanColors = new ArrayList<>();
            for (String color : apiProduct.colors) {
                if (color != null) {
                    // Remove any JSON array brackets or quotes
                    String cleanColor = color.trim()
                        .replaceAll("^\\[\"", "")
                        .replaceAll("\"\\]$", "")
                        .replaceAll("^\"", "")
                        .replaceAll("\"$", "")
                        .replaceAll("^\\[", "")
                        .replaceAll("\\]$", "");
                    if (!cleanColor.isEmpty()) {
                        cleanColors.add(cleanColor);
                    }
                }
            }
            colorsCsv = String.join(",", cleanColors);
        }

        double discountPercent = apiProduct.discountPercent != null ? apiProduct.discountPercent : 0.0;
        
        return new Product(
                apiProduct.id,
                apiProduct.name,
                apiProduct.description != null ? apiProduct.description : "",
                apiProduct.price != null ? apiProduct.price : 0.0,
                discountPercent,
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
    
    public static List<Product> toProductListShuffled(List<ApiProduct> apiProducts) {
        List<Product> products = toProductList(apiProducts);
        Collections.shuffle(products);
        return products;
    }
}

