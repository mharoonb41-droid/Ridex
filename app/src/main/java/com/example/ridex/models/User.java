package com.example.ridex.models;

public class User {

    String name;
    String email;
    String phone;
    String role;

    // Empty constructor (Firebase ke liye zaroori)
    public User() {}

    // Full constructor
    public User(String name, String email, String phone, String role) {
        this.name  = name;
        this.email = email;
        this.phone = phone;
        this.role  = role;
    }

    // Getters
    public String getName()  { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole()  { return role; }

    // Setters
    public void setName(String name)   { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role)   { this.role = role; }
}