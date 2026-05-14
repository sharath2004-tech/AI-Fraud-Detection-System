package com.frauddetector.dto.response;

import com.frauddetector.entity.FraudAlert;

import java.time.LocalDateTime;

public class FraudAlertResponse {

    private Long id;
    private Long transactionId;
    private int riskScore;
    private FraudAlert.Severity severity;
    private FraudAlert.AlertStatus status;
    private Long analystId;
    private String analystName;
    private String notes;
    private boolean falsePositive;
    private LocalDateTime resolutionTime;
    private boolean escalated;
    private LocalDateTime createdAt;

    // Constructor from FraudAlert entity
    public FraudAlertResponse(FraudAlert alert) {
        this.id = alert.getId();
        this.transactionId = alert.getTransaction().getId();
        this.riskScore = alert.getRiskScore();
        this.severity = alert.getSeverity();
        this.status = alert.getStatus();
        this.analystId = alert.getAnalyst() != null ? alert.getAnalyst().getId() : null;
        this.analystName = alert.getAnalyst() != null ? alert.getAnalyst().getName() : null;
        this.notes = alert.getNotes();
        this.falsePositive = alert.isFalsePositive();
        this.resolutionTime = alert.getResolutionTime();
        this.escalated = alert.isEscalated();
        this.createdAt = alert.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public FraudAlert.Severity getSeverity() {
        return severity;
    }

    public void setSeverity(FraudAlert.Severity severity) {
        this.severity = severity;
    }

    public FraudAlert.AlertStatus getStatus() {
        return status;
    }

    public void setStatus(FraudAlert.AlertStatus status) {
        this.status = status;
    }

    public Long getAnalystId() {
        return analystId;
    }

    public void setAnalystId(Long analystId) {
        this.analystId = analystId;
    }

    public String getAnalystName() {
        return analystName;
    }

    public void setAnalystName(String analystName) {
        this.analystName = analystName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isFalsePositive() {
        return falsePositive;
    }

    public void setFalsePositive(boolean falsePositive) {
        this.falsePositive = falsePositive;
    }

    public LocalDateTime getResolutionTime() {
        return resolutionTime;
    }

    public void setResolutionTime(LocalDateTime resolutionTime) {
        this.resolutionTime = resolutionTime;
    }

    public boolean isEscalated() {
        return escalated;
    }

    public void setEscalated(boolean escalated) {
        this.escalated = escalated;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}