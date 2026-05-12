package com.example.solusyoninternetserviceprovider;

public class StaffModel {
    private String name;
    private String role;
    private String node;
    private String imageUrl;

    // Constructor
    public StaffModel(String name, String role, String node, String imageUrl) {
        this.name = name;
        this.role = role;
        this.node = node;
        this.imageUrl = imageUrl;
    }

    // Getters - These MUST exist for the Adapter to work
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getNode() { return node; }
    public String getImageUrl() { return imageUrl; }
}