package com.cybercafe.billing;

import java.util.ArrayList;

/**
 * Tracks products ordered by customer during session.
 */
public class Order {

    private ArrayList<Product> products;

    public Order() {
        products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        products.add(product);
    }

    public double getTotalPrice() {
        double total = 0;
        for (Product p : products) {
            total += p.getPrice();
        }
        return total;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }
}