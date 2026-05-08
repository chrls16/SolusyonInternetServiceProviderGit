package com.example.solusyoninternetserviceprovider;

public class ProblemModel {
    private String title;
    private String description;
    private int iconRes;
    private boolean isSelected = false;

    public ProblemModel(String title, String description, int iconRes) {
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getIconRes() { return iconRes; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}