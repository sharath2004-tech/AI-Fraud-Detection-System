package com.frauddetector.dto.request;

import com.frauddetector.entity.Transaction;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 13, fraction = 2, message = "Amount must have at most 13 integer digits and 2 decimal places")
    private BigDecimal amount;

    @NotNull(message = "Transaction type is required")
    private Transaction.TransactionType transactionType;

    @Size(max = 10, message = "Currency must be less than 10 characters")
    private String currency = "INR";

    @NotBlank(message = "Sender account is required")
    @Size(min = 10, max = 20, message = "Sender account must be between 10 and 20 characters")
    @Pattern(regexp = "\\d+", message = "Sender account must contain only digits")
    private String senderAccount;

    @NotBlank(message = "Receiver account is required")
    @Size(min = 10, max = 20, message = "Receiver account must be between 10 and 20 characters")
    @Pattern(regexp = "\\d+", message = "Receiver account must contain only digits")
    private String receiverAccount;

    @Size(max = 100, message = "Location must be less than 100 characters")
    private String location;

    @Size(max = 50, message = "IP address must be less than 50 characters")
    private String ipAddress;

    @Size(max = 100, message = "Device ID must be less than 100 characters")
    private String deviceId;

    private boolean isInternational = false;

    // Custom validation to ensure sender != receiver
    // This will be implemented in a validator

    // Getters and Setters

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
}