package com.example.nirvana.data.models;

public class Gym {
    private String id;
    private String name;
    private String address;
    private double latitude;
    private double longitude;
    private float rating;
    private String imageUrl;
    private String description;
    private String[] amenities;
    private String[] workingHours;
    private double monthlyFee;
    private String contactNumber;
    private String website;

    public Gym(String id, String name, String address, double latitude, double longitude, 
              float rating, String imageUrl, String description, String[] amenities, 
              String[] workingHours, double monthlyFee, String contactNumber, String website) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.description = description;
        this.amenities = amenities;
        this.workingHours = workingHours;
        this.monthlyFee = monthlyFee;
        this.contactNumber = contactNumber;
        this.website = website;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public float getRating() { return rating; }
    public String getImageUrl() { return imageUrl; }
    public String getDescription() { return description; }
    public String[] getAmenities() { return amenities; }
    public String[] getWorkingHours() { return workingHours; }
    public double getMonthlyFee() { return monthlyFee; }
    public String getContactNumber() { return contactNumber; }
    public String getWebsite() { return website; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setRating(float rating) { this.rating = rating; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setDescription(String description) { this.description = description; }
    public void setAmenities(String[] amenities) { this.amenities = amenities; }
    public void setWorkingHours(String[] workingHours) { this.workingHours = workingHours; }
    public void setMonthlyFee(double monthlyFee) { this.monthlyFee = monthlyFee; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public void setWebsite(String website) { this.website = website; }
} 