package com.sujay.apps.sarvamart;

import java.io.Serializable;

public class ProductsToBuy implements Serializable {
    private String imageUrl;
    private String name;
    private String productId;
    private String idInCart;
    private int quantity;
    private int price;
    private double rating;

    public ProductsToBuy() {
    }

    public ProductsToBuy(String imageUrl, String name, String productId, String idInCart, int quantity, int price) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.productId = productId;
        this.idInCart = idInCart;
        this.quantity = quantity;
        this.price = price;
    }

    public ProductsToBuy(String imageUrl, String name, String productId, int quantity, int price, double rating) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.productId = productId;
        this.rating = rating;
        this.quantity = quantity;
        this.price = price;
    }

    public ProductsToBuy(String imageUrl, String name, String productId, int quantity, int price) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getProductId() {
        return productId;
    }

    public String getIdInCart() {
        return idInCart;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}
