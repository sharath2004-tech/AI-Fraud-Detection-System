package com.frauddetector.dto.response;

import java.time.LocalDate;

public class DailyAnalyticsDTO {
    private LocalDate date;
    private long totalTransactions;
    private long flaggedTransactions;

    public DailyAnalyticsDTO(LocalDate date, long totalTransactions, long flaggedTransactions) {
        this.date = date;
        this.totalTransactions = totalTransactions;
        this.flaggedTransactions = flaggedTransactions;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }
    public long getFlaggedTransactions() { return flaggedTransactions; }
    public void setFlaggedTransactions(long flaggedTransactions) { this.flaggedTransactions = flaggedTransactions; }
}
