package com.example.eccomerceapp.model;

public class CartItem {
    private final long id;
    private final Product product;
    private final int quantity;
    private final String selectedSize;
    private final String selectedColor;

    public CartItem(long id, Product product, int quantity, String selectedSize, String selectedColor) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.selectedSize = selectedSize;
        this.selectedColor = selectedColor;
    }

    public long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSelectedSize() {
        return selectedSize;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

