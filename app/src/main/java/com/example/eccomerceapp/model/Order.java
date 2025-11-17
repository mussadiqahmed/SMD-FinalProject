package com.example.eccomerceapp.model;

public class Order {
    private final long id;
    private final String customerName;
    private final String phone;
    private final String addressLine;
    private final String city;
    private final double totalAmount;
    private final String status;
    private final long createdAt;

    public Order(long id,
                 String customerName,
                 String phone,
                 String addressLine,
                 String city,
                 double totalAmount,
                 String status,
                 long createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.phone = phone;
        this.addressLine = addressLine;
        this.city = city;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}

