package com.frauddetector.service;

import com.frauddetector.dto.request.TransactionRequest;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.entity.Transaction;
import com.frauddetector.entity.User;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.TransactionRepository;
import com.frauddetector.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.frauddetector.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.frauddetector.repository.BlacklistedAccountRepository blacklistedAccountRepository;

    @Autowired
    private FraudScoringService fraudScoringService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public Transaction createTransaction(TransactionRequest request) {
        // Validate sender != receiver
        if (request.getSenderAccount().equals(request.getReceiverAccount())) {
            throw new IllegalArgumentException("Sender and receiver accounts cannot be the same");
        }

        User user = getCurrentUser();

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setAmount(request.getAmount());
        transaction.setTransactionType(request.getTransactionType());
        transaction.setCurrency(request.getCurrency());
        transaction.setSenderAccount(request.getSenderAccount());
        transaction.setReceiverAccount(request.getReceiverAccount());
        transaction.setLocation(request.getLocation());
        transaction.setIpAddress(request.getIpAddress());
        transaction.setDeviceId(request.getDeviceId());
        transaction.setInternational(request.isInternational());

        transaction = transactionRepository.save(transaction);

        // Hydrate context for scoring
        FraudContext context = new FraudContext();
        
        java.time.LocalDateTime tenMinutesAgo = transaction.getCreatedAt().minusMinutes(10);
        java.util.List<Transaction> recentTx = transactionRepository.findByUserAndCreatedAtAfter(user, tenMinutesAgo);
        context.setRecentTransactionCount(recentTx.size());

        if (request.getDeviceId() != null) {
            java.time.LocalDateTime thirtyDaysAgo = transaction.getCreatedAt().minusDays(30);
            java.util.List<Transaction> monthTx = transactionRepository.findByUserAndCreatedAtAfter(user, thirtyDaysAgo);
            boolean newDevice = monthTx.stream().noneMatch(t -> request.getDeviceId().equals(t.getDeviceId()));
            context.setNewDevice(newDevice);
        } else {
            context.setNewDevice(false);
        }

        context.setBlacklisted(blacklistedAccountRepository.existsByAccountNumber(request.getReceiverAccount()));
        context.setHasRecentFailedLogins(user.getFailedLoginAttempts() > 3);

        // Calculate fraud score internally logic
        int riskScore = fraudScoringService.calculateRiskScore(transaction, context);

        // Determine severity and status
        FraudAlert.Severity severity;
        Transaction.TransactionStatus status = Transaction.TransactionStatus.COMPLETED;
        boolean escalated = false;

        if (riskScore >= 81) {
            severity = FraudAlert.Severity.CRITICAL;
            status = Transaction.TransactionStatus.FLAGGED;
            escalated = true;
        } else if (riskScore >= 51) {
            severity = FraudAlert.Severity.HIGH;
            status = Transaction.TransactionStatus.FLAGGED;
        } else if (riskScore >= 21) {
            severity = FraudAlert.Severity.MEDIUM;
        } else {
            severity = FraudAlert.Severity.LOW;
        }

        transaction.setStatus(status);
        transactionRepository.save(transaction);

        // Create alert if score >= 21
        if (riskScore >= 21) {
            FraudAlert alert = new FraudAlert();
            alert.setTransaction(transaction);
            alert.setRiskScore(riskScore);
            alert.setSeverity(severity);
            alert.setEscalated(escalated);
            alert = fraudAlertRepository.save(alert);
            
            // Send email notifications
            if (severity == FraudAlert.Severity.CRITICAL || severity == FraudAlert.Severity.HIGH) {
                try {
                    emailService.sendFraudAlertNotification(alert);
                } catch (Exception e) {
                    log.warn("Failed to send fraud alert email for alert ID: {}", alert.getId());
                }
            }
            
            // Send WebSocket notification for critical alerts
            if (severity == FraudAlert.Severity.CRITICAL) {
                try {
                    messagingTemplate.convertAndSend("/topic/alerts", "Critical Alert ID #" + alert.getId() + " - Score: " + riskScore);
                } catch (Exception e) {
                    // Ignore messaging errors so it doesn't break transaction creation
                }
            }
        }

        return transaction;
    }

    public Page<Transaction> getAllTransactions(Pageable pageable, LocalDateTime startDate, LocalDateTime endDate,
                                               Transaction.TransactionStatus status, Long userId,
                                               BigDecimal minAmount, BigDecimal maxAmount) {
        return transactionRepository.findFilteredTransactions(startDate, endDate, status, userId, minAmount, maxAmount, pageable);
    }

    public Page<Transaction> getMyTransactions(Pageable pageable) {
        User user = getCurrentUser();
        return transactionRepository.findByUser(user, pageable);
    }

    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElseThrow();
    }
}