package com.example.renta;

/**
 * User: Data model representing a user in the application.
 * Stores essential profile information such as name, email, and password.
 */
public class User {
    private String name;
    private String email;
    private String password;

    /**
     * Constructor for basic user creation with a default password.
     */
    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.password = "123456"; // Default password for placeholder accounts
    }

    /**
     * Constructor for creating a user with a specific password.
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters with brief descriptions

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
