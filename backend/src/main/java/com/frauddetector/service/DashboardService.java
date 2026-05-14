package com.frauddetector.service;

import com.frauddetector.dto.response.DashboardMetricsDTO;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    public DashboardMetricsDTO getMetrics() {
        long totalTransactions = transactionRepository.count();
        long flaggedTransactions = transactionRepository.countByStatus(com.frauddetector.entity.Transaction.TransactionStatus.FLAGGED);

        long totalAlerts = fraudAlertRepository.count();
        long unresolvedAlerts = fraudAlertRepository.countByStatus(FraudAlert.AlertStatus.OPEN) + 
                                fraudAlertRepository.countByStatus(FraudAlert.AlertStatus.UNDER_REVIEW);
        long criticalAlerts = fraudAlertRepository.countBySeverity(FraudAlert.Severity.CRITICAL);

        return new DashboardMetricsDTO(totalTransactions, flaggedTransactions, totalAlerts, unresolvedAlerts, criticalAlerts);
    }

    public java.util.List<com.frauddetector.dto.response.DailyAnalyticsDTO> getDailyAnalytics() {
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.util.List<Object[]> raw = transactionRepository.getDailyAnalyticsNative(thirtyDaysAgo);
        return raw.stream().map(row -> new com.frauddetector.dto.response.DailyAnalyticsDTO(
                ((java.sql.Date) row[0]).toLocalDate(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue()
        )).collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<com.frauddetector.dto.response.MonthlyAnalyticsDTO> getMonthlyAnalytics() {
        java.time.LocalDateTime twelveMonthsAgo = java.time.LocalDateTime.now().minusMonths(12);
        java.util.List<Object[]> raw = transactionRepository.getMonthlyAnalyticsNative(twelveMonthsAgo);
        return raw.stream().map(row -> new com.frauddetector.dto.response.MonthlyAnalyticsDTO(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue()
        )).collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getRiskDistribution() {
        java.util.Map<String, Long> map = new java.util.HashMap<>();
        map.put("LOW", fraudAlertRepository.countBySeverity(FraudAlert.Severity.LOW));
        map.put("MEDIUM", fraudAlertRepository.countBySeverity(FraudAlert.Severity.MEDIUM));
        map.put("HIGH", fraudAlertRepository.countBySeverity(FraudAlert.Severity.HIGH));
        map.put("CRITICAL", fraudAlertRepository.countBySeverity(FraudAlert.Severity.CRITICAL));
        return map;
    }

    public java.util.List<com.frauddetector.dto.response.TopFlaggedUserDTO> getTopFlaggedUsers() {
        return transactionRepository.getTopFlaggedUsers(org.springframework.data.domain.PageRequest.of(0, 5));
    }
}
