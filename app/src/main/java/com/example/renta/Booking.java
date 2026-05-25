package com.example.renta;

import java.io.Serializable;

/**
 * Booking: A data model representing a car rental booking.
 * Implements Serializable so it can be passed between activities/fragments if needed.
 */
public class Booking implements Serializable {
    private String carName;      // Name of the car (e.g., Toyota Vios)
    private String carPrice;     // Daily rate string
    private long startDate;      // Start date of rental in milliseconds
    private long endDate;        // End date of rental in milliseconds
    private double totalCost;    // Total calculated price
    private double downPayment;  // Initial payment amount
    private String paymentMethod;// Method used (e.g., GCash, PayMaya)
    private String phoneNumber;  // User's contact number

    public Booking(String carName, String carPrice, long startDate, long endDate, double totalCost, 
                   double downPayment, String paymentMethod, String phoneNumber) {
        this.carName = carName;
        this.carPrice = carPrice;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalCost = totalCost;
        this.downPayment = downPayment;
        this.paymentMethod = paymentMethod;
        this.phoneNumber = phoneNumber;
    }

    // Standard getters for accessing booking data
    public String getCarName() { return carName; }
    public String getCarPrice() { return carPrice; }
    public long getStartDate() { return startDate; }
    public long getEndDate() { return endDate; }
    public double getTotalCost() { return totalCost; }
    public double getDownPayment() { return downPayment; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPhoneNumber() { return phoneNumber; }
}
