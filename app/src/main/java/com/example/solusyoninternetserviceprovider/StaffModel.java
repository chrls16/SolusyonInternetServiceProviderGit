package com.example.solusyoninternetserviceprovider;

import java.io.Serializable;

public class StaffModel implements Serializable { // Serializable allows passing the object via Intent
    private String name, email, phone, birthdate, sex, address, role, imageUrl, node, staffId;

    public StaffModel() {} // Required for Firebase

    public StaffModel(String name, String email, String phone, String birthdate, String sex, String address, String role, String imageUrl, String node) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.birthdate = birthdate;
        this.sex = sex;
        this.address = address;
        this.role = role;
        this.imageUrl = imageUrl;
        this.node = node;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBirthdate() { return birthdate; }
    public void setBirthdate(String birthdate) { this.birthdate = birthdate; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
}