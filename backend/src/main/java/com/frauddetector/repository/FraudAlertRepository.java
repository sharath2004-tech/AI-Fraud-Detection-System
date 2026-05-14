package com.frauddetector.repository;

import com.frauddetector.entity.FraudAlert;
import com.frauddetector.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    Page<FraudAlert> findByStatus(FraudAlert.AlertStatus status, Pageable pageable);

    @Query("SELECT f FROM FraudAlert f WHERE " +
           "(:status IS NULL OR f.status = :status) AND " +
           "(:severity IS NULL OR f.severity = :severity) AND " +
           "(:analystId IS NULL OR f.analyst.id = :analystId) AND " +
           "(:startDate IS NULL OR f.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR f.createdAt <= :endDate)")
    Page<FraudAlert> findFilteredAlerts(
            @Param("status") FraudAlert.AlertStatus status,
            @Param("severity") FraudAlert.Severity severity,
            @Param("analystId") Long analystId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByStatus(FraudAlert.AlertStatus status);

    long countBySeverity(FraudAlert.Severity severity);
}