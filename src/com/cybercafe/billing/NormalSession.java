package com.cybercafe.billing;

/**
 * A normal session with standard billing.
 */
public class NormalSession extends Session {

    public NormalSession(String customerName, double ratePerHour) {
        super(customerName, ratePerHour);
    }

    @Override
    public double calculateBill() {
        double hours = (double) getDurationInMinutes() / 60.0;
        double bill = hours * ratePerHour; // ratePerHour from superclass
        return Math.round(bill * 100.0) / 100.0; // round to 2 decimals
    }
}