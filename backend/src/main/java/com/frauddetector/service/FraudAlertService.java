package com.frauddetector.service;
import com.frauddetector.dto.request.AssignAlertRequest;
import com.frauddetector.dto.request.ResolveAlertRequest;
import com.frauddetector.dto.request.EscalateAlertRequest;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.entity.User;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.frauddetector.exception.ResourceNotFoundException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FraudAlertService {

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogService auditLogService;

    public Page<FraudAlert> getAllAlerts(Pageable pageable, FraudAlert.AlertStatus status, FraudAlert.Severity severity,
                                        Long analystId, LocalDateTime startDate, LocalDateTime endDate) {
        return fraudAlertRepository.findFilteredAlerts(status, severity, analystId, startDate, endDate, pageable);
    }

    public FraudAlert getAlertById(Long id) {
        return fraudAlertRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));
    }

    @Transactional
    public FraudAlert assignAlert(Long alertId, AssignAlertRequest request) {
        FraudAlert alert = getAlertById(alertId);
        User analyst = userRepository.findById(request.getAnalystId()).orElseThrow();
        alert.setAnalyst(analyst);
        alert.setStatus(FraudAlert.AlertStatus.UNDER_REVIEW);
        return fraudAlertRepository.save(alert);
    }

    @Transactional
    public FraudAlert resolveAlert(Long alertId, ResolveAlertRequest request) {
        FraudAlert alert = getAlertById(alertId);
        alert.setNotes(request.getNotes());
        alert.setFalsePositive(request.isFalsePositive());
        alert.setStatus(FraudAlert.AlertStatus.RESOLVED);
        alert.setResolutionTime(LocalDateTime.now());
        alert = fraudAlertRepository.save(alert);
        
        auditLogService.log(null, "ALERT_RESOLVED", "FraudAlert", alertId, "System");
        return alert;
    }

    @Transactional
    public FraudAlert markFalsePositive(Long alertId, ResolveAlertRequest request) {
        FraudAlert alert = getAlertById(alertId);
        alert.setNotes(request.getNotes());
        alert.setFalsePositive(true);
        alert.setStatus(FraudAlert.AlertStatus.FALSE_POSITIVE);
        alert.setResolutionTime(LocalDateTime.now());
        alert = fraudAlertRepository.save(alert);
        
        auditLogService.log(null, "ALERT_FALSE_POSITIVE", "FraudAlert", alertId, "System");
        return alert;
    }

    @Transactional
    public FraudAlert escalateAlert(Long alertId, EscalateAlertRequest request) {
        FraudAlert alert = getAlertById(alertId);
        alert.setNotes(request.getNotes());
        alert.setEscalated(true);
        alert = fraudAlertRepository.save(alert);
        
        auditLogService.log(null, "ALERT_ESCALATED", "FraudAlert", alertId, "System");
        return alert;
    }
}