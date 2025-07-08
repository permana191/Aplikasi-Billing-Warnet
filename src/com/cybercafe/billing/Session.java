package com.cybercafe.billing;

/**
 * Abstract base class representing a Cybercafe session.
 * Demonstrates Abstraction and Encapsulation.
 */
public abstract class Session {
    // Encapsulated fields, private for data hiding
    private String customerName;
    private long startTime; // Session start timestamp in milliseconds
    private long endTime;   // Session end time (0 if ongoing)
    protected double ratePerHour;

    // Constructor initializes encapsulated fields
    public Session(String customerName, double ratePerHour) {
        this.customerName = customerName;
        this.ratePerHour = ratePerHour;
        this.startTime = System.currentTimeMillis();
        this.endTime = 0; // 0 indicates session still active
    }

    public String getCustomerName() {
        return customerName;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void endSession() {
        this.endTime = System.currentTimeMillis();
    }

    public long getDurationInMinutes() {
        long end = (endTime == 0) ? System.currentTimeMillis() : endTime;
        long duration = (end - startTime) / 60000;
        if (duration == 0) {
            return 1; // minimum 1 minute charged
        }
        return duration;
    }


    public String getDurationFormatted() {
        long mins = getDurationInMinutes();
        long hours = mins / 60;
        long minutes = mins % 60;
        return hours + " hours " + minutes + " minutes";
    }

    // Abstract method for calculating bill, subclasses provide implementation (Polymorphism)
    public abstract double calculateBill();
}
