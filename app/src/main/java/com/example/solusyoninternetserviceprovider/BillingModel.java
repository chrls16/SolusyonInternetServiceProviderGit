package com.example.solusyoninternetserviceprovider;

import java.io.Serializable;

public class BillingModel implements Serializable {
    private String userId;
    private String name;
    private String accountNo;
    private String planName;
    private String planSpeed;
    private String planType;
    private String status;
    private String price;
    private String date;
    private boolean isExpanded = false;

    public BillingModel() {} // Required for Firebase

    public BillingModel(String userId, String name, String accountNo, String planName, String planSpeed, String planType, String status, String price, String date) {
        this.userId = userId;
        this.name = name;
        this.accountNo = accountNo;
        this.planName = planName;
        this.planSpeed = planSpeed;
        this.planType = planType;
        this.status = status;
        this.price = price;
        this.date = date;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getAccountNo() { return accountNo; }
    public String getPlanName() { return planName; }
    public String getPlanSpeed() { return planSpeed; }
    public String getPlanType() { return planType; }
    public String getStatus() { return status; }
    public String getPrice() { return price; }
    public String getDate() { return date; }
    public boolean isExpanded() { return isExpanded; }

    // Setters
    public void setUserId(String userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    /**
     * Helper to generate initials for the avatar circle (e.g., "John Doe" -> "JD")
     */
    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "";
        String[] parts = name.trim().split(" ");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return name.substring(0, 1).toUpperCase();
        }
    }
}