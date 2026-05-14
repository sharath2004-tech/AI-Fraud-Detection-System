package com.frauddetector.dto.response;

import com.frauddetector.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private Transaction.TransactionType transactionType;
    private String currency;
    private String senderAccount;
    private String receiverAccount;
    private String location;
    private String ipAddress;
    private String deviceId;
    private boolean isInternational;
    private Transaction.TransactionStatus status;
    private LocalDateTime createdAt;

    // Constructor from Transaction entity
    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.userId = transaction.getUser().getId();
        this.userName = transaction.getUser().getName();
        this.amount = transaction.getAmount();
        this.transactionType = transaction.getTransactionType();
        this.currency = transaction.getCurrency();
        this.senderAccount = transaction.getSenderAccount();
        this.receiverAccount = transaction.getReceiverAccount();
        this.location = transaction.getLocation();
        this.ipAddress = transaction.getIpAddress();
        this.deviceId = transaction.getDeviceId();
        this.isInternational = transaction.isInternational();
        this.status = transaction.getStatus();
        this.createdAt = transaction.getCreatedAt();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Transaction.TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(Transaction.TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isInternational() {
        return isInternational;
    }

    public void setInternational(boolean isInternational) {
        this.isInternational = isInternational;
    }

    public Transaction.TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(Transaction.TransactionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}