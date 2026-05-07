package com.example.ridex.models;

public class RideRequest {

    private String riderId;
    private String driverId;
    private String destination;
    private double riderLat;
    private double riderLng;
    private double driverLat;
    private double driverLng;
    private String status;
    private long timestamp;

    // Empty constructor (Firebase ke liye zaroori)
    public RideRequest() {}

    // Full constructor
    public RideRequest(String riderId, String destination,
                       double riderLat, double riderLng) {
        this.riderId     = riderId;
        this.driverId    = "";
        this.destination = destination;
        this.riderLat    = riderLat;
        this.riderLng    = riderLng;
        this.driverLat   = 0.0;
        this.driverLng   = 0.0;
        this.status      = "pending";
        this.timestamp   = System.currentTimeMillis();
    }

    // Getters
    public String getRiderId()     { return riderId; }
    public String getDriverId()    { return driverId; }
    public String getDestination() { return destination; }
    public double getRiderLat()    { return riderLat; }
    public double getRiderLng()    { return riderLng; }
    public double getDriverLat()   { return driverLat; }
    public double getDriverLng()   { return driverLng; }
    public String getStatus()      { return status; }
    public long getTimestamp()     { return timestamp; }

    // Setters
    public void setRiderId(String riderId)       { this.riderId = riderId; }
    public void setDriverId(String driverId)     { this.driverId = driverId; }
    public void setDestination(String dest)      { this.destination = dest; }
    public void setRiderLat(double riderLat)     { this.riderLat = riderLat; }
    public void setRiderLng(double riderLng)     { this.riderLng = riderLng; }
    public void setDriverLat(double driverLat)   { this.driverLat = driverLat; }
    public void setDriverLng(double driverLng)   { this.driverLng = driverLng; }
    public void setStatus(String status)         { this.status = status; }
    public void setTimestamp(long timestamp)     { this.timestamp = timestamp; }
}