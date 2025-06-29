package com.cybercafe.billing;

/**
 * VIPDiscountSession provides billing with discounts.
 */
public class VIPDiscountSession extends Session {

    private double discountRate; // Example: 0.2 means 20%

    public VIPDiscountSession(String customerName, double ratePerHour, double discountRate) {
        super(customerName, ratePerHour);
        this.discountRate = discountRate;
    }

    public double getDiscountRate() {
        return discountRate;
    }

    @Override
    public double calculateBill() {
        double hours = (double) getDurationInMinutes() / 60.0;
        double gross = hours * ratePerHour;
        double net = gross * (1 - discountRate);
        return Math.round(net * 100.0) / 100.0;
    }
}
