package com.example.solusyoninternetserviceprovider;

public class BillingModel {
    private String name, accountNo, planName, planSpeed, planType, status, price, date;

    public BillingModel() {}

    public BillingModel(String name, String accountNo, String planName, String planSpeed, String planType, String status, String price, String date) {
        this.name = name;
        this.accountNo = accountNo;
        this.planName = planName;
        this.planSpeed = planSpeed;
        this.planType = planType;
        this.status = status;
        this.price = price;
        this.date = date;
    }

    public String getName() { return name; }
    public String getAccountNo() { return accountNo; }
    public String getPlanName() { return planName; }
    public String getPlanSpeed() { return planSpeed; }
    public String getPlanType() { return planType; }
    public String getStatus() { return status; }

    public String getPrice() { return price; }
    public String getDate() { return date; }


    public String getInitials() {
        if (name == null || name.isEmpty()) return "";
        String[] parts = name.split(" ");
        return (parts.length > 1) ? (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase() : name.substring(0, 1).toUpperCase();
    }
}