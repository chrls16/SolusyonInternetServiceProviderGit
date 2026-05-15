package com.example.solusyoninternetserviceprovider;

import java.io.Serializable;

public class PlanModel implements Serializable {
    private String planId;
    private String name;
    private String price;
    private String speed;
    private String upload;
    private int iconRes;
    private int colorRes;

    public PlanModel() {} // Required for Firebase

    public PlanModel(String name, String price, String speed, String upload, int iconRes, int colorRes) {
        this.name = name;
        this.price = price;
        this.speed = speed;
        this.upload = upload;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getSpeed() { return speed; }
    public void setSpeed(String speed) { this.speed = speed; }
    public String getUpload() { return upload; }
    public void setUpload(String upload) { this.upload = upload; }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
    public int getColorRes() { return colorRes; }
    public void setColorRes(int colorRes) { this.colorRes = colorRes; }
}