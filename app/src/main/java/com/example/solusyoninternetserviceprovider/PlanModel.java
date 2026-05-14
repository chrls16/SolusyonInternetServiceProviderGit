package com.example.solusyoninternetserviceprovider;

public class PlanModel {
    private String name;
    private String price;
    private String speed;
    private String upload;
    private int iconRes;
    private int colorRes;

    public PlanModel(String name, String price, String speed, String upload, int iconRes, int colorRes) {
        this.name = name;
        this.price = price;
        this.speed = speed;
        this.upload = upload;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
    }

    // Getters
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getSpeed() { return speed; }
    public String getUpload() { return upload; }
    public int getIconRes() { return iconRes; }
    public int getColorRes() { return colorRes; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setSpeed(String speed) { this.speed = speed; }
    public void setUpload(String upload) { this.upload = upload; }
}