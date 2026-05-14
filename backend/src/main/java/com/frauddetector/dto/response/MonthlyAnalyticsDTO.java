package com.frauddetector.dto.response;

public class MonthlyAnalyticsDTO {
    private String month;
    private long totalTransactions;
    private long flaggedTransactions;

    public MonthlyAnalyticsDTO(String month, long totalTransactions, long flaggedTransactions) {
        this.month = month;
        this.totalTransactions = totalTransactions;
        this.flaggedTransactions = flaggedTransactions;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public long getFlaggedTransactions() { return flaggedTransactions; }
    public void setFlaggedTransactions(long flaggedTransactions) { this.flaggedTransactions = flaggedTransactions; }
}
