package com.example.renta;

import java.io.Serializable;

public class Booking implements Serializable {
    private String bookingId;
    private String userId;
    private String carName;
    private String carPrice;
    private long startDate;
    private long endDate;
    private double totalCost;
    private double downPayment;
    private String paymentMethod;
    private String phoneNumber;
    private String customerName;
    private String address;
    private String licenseNumber;
    private String status; // e.g., "Pending", "Confirmed", "Cancelled"
    private boolean isGuestBooking;

    public Booking() {
    }

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
        this.status = "Pending";
    }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getCarName() { return carName; }
    public void setCarName(String carName) { this.carName = carName; }

    public String getCarPrice() { return carPrice; }
    public void setCarPrice(String carPrice) { this.carPrice = carPrice; }

    public long getStartDate() { return startDate; }
    public void setStartDate(long startDate) { this.startDate = startDate; }

    public long getEndDate() { return endDate; }
    public void setEndDate(long endDate) { this.endDate = endDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getDownPayment() { return downPayment; }
    public void setDownPayment(double downPayment) { this.downPayment = downPayment; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isGuestBooking() { return isGuestBooking; }
    public void setGuestBooking(boolean guestBooking) { isGuestBooking = guestBooking; }
}
