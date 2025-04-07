package com.example.nirvana.models;

import com.google.firebase.database.PropertyName;
import java.util.Date;

public class GymMembership {
    private String id;
    private String userId;
    private String gymId;
    private String gymName;
    private String membershipType;
    private String status;
    private Date startDate;
    private Date endDate;
    private double price;

    public GymMembership() {
        // Required empty constructor for Firebase
    }

    public GymMembership(String id, String userId, String gymId, String gymName,
                        String membershipType, String status, Date startDate,
                        Date endDate, double price) {
        this.id = id;
        this.userId = userId;
        this.gymId = gymId;
        this.gymName = gymName;
        this.membershipType = membershipType;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("user_id")
    public String getUserId() {
        return userId;
    }

    @PropertyName("user_id")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("gym_id")
    public String getGymId() {
        return gymId;
    }

    @PropertyName("gym_id")
    public void setGymId(String gymId) {
        this.gymId = gymId;
    }

    @PropertyName("gym_name")
    public String getGymName() {
        return gymName;
    }

    @PropertyName("gym_name")
    public void setGymName(String gymName) {
        this.gymName = gymName;
    }

    @PropertyName("membership_type")
    public String getMembershipType() {
        return membershipType;
    }

    @PropertyName("membership_type")
    public void setMembershipType(String membershipType) {
        this.membershipType = membershipType;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("start_date")
    public Date getStartDate() {
        return startDate;
    }

    @PropertyName("start_date")
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @PropertyName("end_date")
    public Date getEndDate() {
        return endDate;
    }

    @PropertyName("end_date")
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @PropertyName("price")
    public double getPrice() {
        return price;
    }

    @PropertyName("price")
    public void setPrice(double price) {
        this.price = price;
    }
} 