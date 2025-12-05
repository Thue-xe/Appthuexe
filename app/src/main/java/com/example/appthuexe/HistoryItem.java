package com.example.appthuexe;

public class HistoryItem {
    public String vehicleName;
    public String startDate;
    public String endDate;
    public long amount;
    public String createdAt;

    public HistoryItem() {}

    public HistoryItem(String vehicleName, String startDate, String endDate,
                       long amount, String createdAt) {
        this.vehicleName = vehicleName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.createdAt = createdAt;
    }
}
