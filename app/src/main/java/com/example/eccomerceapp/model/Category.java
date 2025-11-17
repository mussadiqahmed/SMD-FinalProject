package com.example.eccomerceapp.model;

public class Category {
    private final long id;
    private final String title;
    private final String imageUrl;

    public Category(long id, String title, String imageUrl) {
        this.id = id;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}

