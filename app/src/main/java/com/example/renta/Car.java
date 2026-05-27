package com.example.renta;

import java.io.Serializable;

public class Car implements Serializable {
    private String id;
    private String name;
    private String price;
    private String description;
    private String fuel;
    private String seats;
    private String transmission;
    private String condition;
    private int imageResId; // Optional: can be a URL string later
    private boolean available;

    public Car() {
        // Required for Firebase
    }

    public Car(String id, String name, String price, String description, String fuel, String seats, String transmission, String condition, int imageResId, boolean available) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.fuel = fuel;
        this.seats = seats;
        this.transmission = transmission;
        this.condition = condition;
        this.imageResId = imageResId;
        this.available = available;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFuel() { return fuel; }
    public void setFuel(String fuel) { this.fuel = fuel; }
    public String getSeats() { return seats; }
    public void setSeats(String seats) { this.seats = seats; }
    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
}
