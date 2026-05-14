package com.example.solusyoninternetserviceprovider;

public class AnnouncementModel {
    private String category;
    private String title;
    private String description;
    private String timestamp;
    private String colorHex;

    // Required empty constructor for Firebase
    public AnnouncementModel() {}

    public AnnouncementModel(String category, String title, String description, String timestamp, String colorHex) {
        this.category = category;
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.colorHex = colorHex;
    }

    // Getters and Setters for all fields
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
}