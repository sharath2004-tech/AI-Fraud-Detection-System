package com.frauddetector.dto.response;

public class DashboardMetricsDTO {
    private long totalTransactions;
    private long flaggedTransactions;
    private long totalAlerts;
    private long unresolvedAlerts;
    private long criticalAlerts;

    public DashboardMetricsDTO(long totalTransactions, long flaggedTransactions, long totalAlerts, long unresolvedAlerts, long criticalAlerts) {
        this.totalTransactions = totalTransactions;
        this.flaggedTransactions = flaggedTransactions;
        this.totalAlerts = totalAlerts;
        this.unresolvedAlerts = unresolvedAlerts;
        this.criticalAlerts = criticalAlerts;
    }

    public long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public long getFlaggedTransactions() {
        return flaggedTransactions;
    }

    public void setFlaggedTransactions(long flaggedTransactions) {
        this.flaggedTransactions = flaggedTransactions;
    }

    public long getTotalAlerts() {
        return totalAlerts;
    }

    public void setTotalAlerts(long totalAlerts) {
        this.totalAlerts = totalAlerts;
    }

    public long getUnresolvedAlerts() {
        return unresolvedAlerts;
    }

    public void setUnresolvedAlerts(long unresolvedAlerts) {
        this.unresolvedAlerts = unresolvedAlerts;
    }

    public long getCriticalAlerts() {
        return criticalAlerts;
    }

    public void setCriticalAlerts(long criticalAlerts) {
        this.criticalAlerts = criticalAlerts;
    }
}
