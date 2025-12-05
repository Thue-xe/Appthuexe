package com.example.appthuexe;

public class Voucher {
    private String code;
    private String title;
    private String description;
    private int discountPercent;
    private int quantity;
    private String startDate;
    private String endDate;

    public Voucher() {}

    public Voucher(String code, String title, String description, int discountPercent,
                   int quantity, String startDate, String endDate) {
        this.code = code;
        this.title = title;
        this.description = description;
        this.discountPercent = discountPercent;
        this.quantity = quantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getCode() { return code; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getDiscountPercent() { return discountPercent; }
    public int getQuantity() { return quantity; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
}
