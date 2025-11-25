package com.example.eccomerceapp.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private final long id;
    private final String name;
    private final String description;
    private final double price;
    private final double discountPercent;
    private final String imageUrl;
    private final long categoryId;
    private final List<String> sizes;
    private final List<String> colors;

    public Product(long id,
                   String name,
                   String description,
                   double price,
                   double discountPercent,
                   String imageUrl,
                   long categoryId,
                   String sizesCsv,
                   String colorsCsv) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.discountPercent = discountPercent;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.sizes = parseCsv(sizesCsv);
        this.colors = parseCsv(colorsCsv);
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Handle JSON array format like ["S","M","L"]
        String cleaned = csv.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            try {
                // Remove brackets and split by comma
                cleaned = cleaned.substring(1, cleaned.length() - 1);
            } catch (Exception e) {
                // Fall through to normal parsing
            }
        }
        
        String[] parts = cleaned.split(",");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            // Remove quotes if present
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (!trimmed.isEmpty()) {
                values.add(trimmed);
            }
        }
        return values;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public double getDiscountedPrice() {
        if (discountPercent > 0) {
            return price - (price * discountPercent / 100.0);
        }
        return price;
    }

    public boolean hasDiscount() {
        return discountPercent > 0;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public List<String> getColors() {
        return colors;
    }
}

