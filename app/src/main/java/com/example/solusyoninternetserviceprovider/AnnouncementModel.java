package com.example.solusyoninternetserviceprovider;

public class AnnouncementModel {
    private String category;
    private String title;
    private String description;
    private String timestamp;
    private String colorHex;
    private int iconRes;

    public AnnouncementModel(String category, String title, String description, String timestamp, String colorHex, int iconRes) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.colorHex = colorHex;
        this.iconRes = iconRes;
    }

    public String getCategory() { return category; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTimestamp() { return timestamp; }
    public String getColorHex() { return colorHex; }
    public int getIconRes() { return iconRes; }
}