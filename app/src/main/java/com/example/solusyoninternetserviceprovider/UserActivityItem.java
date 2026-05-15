package com.example.solusyoninternetserviceprovider;

public class UserActivityItem {
    private String title;
    private String invoiceId;
    private String amount;
    private String status;

    public UserActivityItem() {} // Required for Firebase

    public UserActivityItem(String title, String invoiceId, String amount, String status) {
        this.title = title;
        this.invoiceId = invoiceId;
        this.amount = amount;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
