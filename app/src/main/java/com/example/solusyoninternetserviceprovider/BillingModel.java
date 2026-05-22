package com.example.solusyoninternetserviceprovider;

import java.io.Serializable;

public class BillingModel implements Serializable {
    private String userId, name, accountNo, planName, planSpeed, planType, status, price, date, billingDate;
    private boolean isExpanded = false;

    public BillingModel() {}

    public BillingModel(String userId, String name, String accountNo, String planName, String planSpeed, String planType, String status, String price, String date, String billingDate) {
        this.userId = userId;
        this.name = name;
        this.accountNo = accountNo;
        this.planName = planName;
        this.planSpeed = planSpeed;
        this.planType = planType;
        this.status = status;
        this.price = price;
        this.date = date;
        this.billingDate = billingDate;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getAccountNo() { return accountNo; }
    public String getPlanName() { return planName; }
    public String getPlanSpeed() { return planSpeed; }
    public String getPlanType() { return planType; }
    public String getStatus() { return status; }
    public String getPrice() { return price; }
    public String getDate() { return date; }
    public String getBillingDate() { return billingDate; }
    public boolean isExpanded() { return isExpanded; }

    public void setUserId(String userId) { this.userId = userId; }
    public void setStatus(String status) { this.status = status; }
    public void setExpanded(boolean expanded) { isExpanded = expanded; }

    public String getInitials() {
        if (name == null || name.trim().isEmpty()) return "??";
        String[] parts = name.trim().split(" ");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        } else {
            return name.substring(0, 1).toUpperCase();
        }
    }
}