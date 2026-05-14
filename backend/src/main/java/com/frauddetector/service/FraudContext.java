package com.frauddetector.service;

public class FraudContext {
    private int recentTransactionCount;
    private boolean isNewDevice;
    private boolean isBlacklisted;
    private boolean hasRecentFailedLogins;

    public FraudContext() {}

    public int getRecentTransactionCount() {
        return recentTransactionCount;
    }

    public void setRecentTransactionCount(int recentTransactionCount) {
        this.recentTransactionCount = recentTransactionCount;
    }

    public boolean isNewDevice() {
        return isNewDevice;
    }

    public void setNewDevice(boolean newDevice) {
        isNewDevice = newDevice;
    }

    public boolean isBlacklisted() {
        return isBlacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        isBlacklisted = blacklisted;
    }

    public boolean isHasRecentFailedLogins() {
        return hasRecentFailedLogins;
    }

    public void setHasRecentFailedLogins(boolean hasRecentFailedLogins) {
        this.hasRecentFailedLogins = hasRecentFailedLogins;
    }
}
