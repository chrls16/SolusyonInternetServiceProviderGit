package com.example.solusyoninternetserviceprovider;

public class TransactionModel {
    private String title;
    private String sub;
    private String amount;

    public TransactionModel() {}

    public TransactionModel(String title, String sub, String amount) {
        this.title = title;
        this.sub = sub;
        this.amount = amount;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSub() { return sub; }
    public void setSub(String sub) { this.sub = sub; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
}