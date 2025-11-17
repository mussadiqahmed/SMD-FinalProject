package com.example.eccomerceapp.model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private final long id;
    private final String name;
    private final String description;
    private final double price;
    private final String imageUrl;
    private final long categoryId;
    private final List<String> sizes;
    private final List<String> colors;

    public Product(long id,
                   String name,
                   String description,
                   double price,
                   String imageUrl,
                   long categoryId,
                   String sizesCsv,
                   String colorsCsv) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.sizes = parseCsv(sizesCsv);
        this.colors = parseCsv(colorsCsv);
    }

    private List<String> parseCsv(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = csv.split(",");
        List<String> values = new ArrayList<>();
        for (String part : parts) {
            values.add(part.trim());
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

