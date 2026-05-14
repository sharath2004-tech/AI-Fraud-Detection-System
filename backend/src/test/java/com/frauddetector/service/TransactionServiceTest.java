package com.frauddetector.service;

import com.frauddetector.dto.request.TransactionRequest;
import com.frauddetector.entity.Transaction;
import com.frauddetector.entity.User;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.TransactionRepository;
import com.frauddetector.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FraudScoringService fraudScoringService;
    
    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        // Setup Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        
        when(authentication.getName()).thenReturn("user@example.com");
    }

    @Test
    void testCreateTransaction_Success() {
        TransactionRequest req = new TransactionRequest();
        req.setSenderAccount("12345");
        req.setReceiverAccount("67890");
        req.setAmount(new BigDecimal("100.00"));
        
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudScoringService.calculateRiskScore(any(Transaction.class), any(FraudContext.class))).thenReturn(10); // Low risk

        Transaction result = transactionService.createTransaction(req);
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.getStatus());
        verify(fraudAlertRepository, never()).save(any());
    }

    @Test
    void testCreateTransaction_HighRisk() {
        TransactionRequest req = new TransactionRequest();
        req.setSenderAccount("12345");
        req.setReceiverAccount("67890");
        req.setAmount(new BigDecimal("10000.00"));
        
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fraudScoringService.calculateRiskScore(any(Transaction.class), any(FraudContext.class))).thenReturn(85); // Critical risk

        Transaction result = transactionService.createTransaction(req);
        
        assertNotNull(result);
        assertEquals(Transaction.TransactionStatus.FLAGGED, result.getStatus());
        verify(fraudAlertRepository, times(1)).save(any());
    }

    @Test
    void testCreateTransaction_SameAccountFails() {
        TransactionRequest req = new TransactionRequest();
        req.setSenderAccount("12345");
        req.setReceiverAccount("12345");
        
        assertThrows(IllegalArgumentException.class, () -> {
            transactionService.createTransaction(req);
        });
    }
}
