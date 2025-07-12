package com.sujay.apps.sarvamart;

import java.util.List;

public class Order {
    private List<ProductsToBuy> products;
    private Address address;
    private int totalAmount;
    private String status;
    private long timestamp;

    public Order(){};

    public Order(List<ProductsToBuy> products, Address address, int totalAmount, String status, long timestamp) {
        this.products = products;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public List<ProductsToBuy> getProducts() {
        return products;
    }

    public Address getAddress() {
        return address;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
