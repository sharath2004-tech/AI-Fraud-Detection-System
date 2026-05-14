package com.frauddetector.service;

import com.frauddetector.entity.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FraudScoringService {

    public int calculateRiskScore(Transaction transaction, FraudContext context) {
        int score = 0;

        // Very High Amount
        if (transaction.getAmount().compareTo(BigDecimal.valueOf(500000)) > 0) {
            score += 50;
        } else if (transaction.getAmount().compareTo(BigDecimal.valueOf(100000)) > 0) {
            score += 30;
        }

        // Velocity Breach
        if (context.getRecentTransactionCount() >= 5) {
            score += 25;
        }

        // Night Transaction
        if (isNightTransaction(transaction.getCreatedAt())) {
            score += 15;
        }

        // New Device
        if (context.isNewDevice()) {
            score += 20;
        }

        // Blacklisted Receiver
        if (context.isBlacklisted()) {
            score += 40;
        }

        // International Transfer
        if (transaction.isInternational()) {
            score += 20;
        }

        // Repeated Failed Logins
        if (context.isHasRecentFailedLogins()) {
            score += 15;
        }

        return score;
    }

    private boolean isNightTransaction(LocalDateTime createdAt) {
        int hour = createdAt.getHour();
        return hour >= 1 && hour <= 5;
    }
}