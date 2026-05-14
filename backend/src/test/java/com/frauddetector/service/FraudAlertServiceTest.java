package com.frauddetector.service;

import com.frauddetector.dto.request.ResolveAlertRequest;
import com.frauddetector.entity.FraudAlert;
import com.frauddetector.repository.FraudAlertRepository;
import com.frauddetector.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FraudAlertServiceTest {

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private FraudAlertService fraudAlertService;

    @Test
    void testResolveAlert() {
        Long alertId = 1L;
        ResolveAlertRequest request = new ResolveAlertRequest();
        request.setNotes("Investigated properly.");
        request.setFalsePositive(false);

        FraudAlert alert = new FraudAlert();
        alert.setId(alertId);
        alert.setStatus(FraudAlert.AlertStatus.UNDER_REVIEW);

        when(fraudAlertRepository.findById(alertId)).thenReturn(Optional.of(alert));
        when(fraudAlertRepository.save(any(FraudAlert.class))).thenAnswer(i -> i.getArguments()[0]);

        FraudAlert resolvedAlert = fraudAlertService.resolveAlert(alertId, request);

        assertEquals(FraudAlert.AlertStatus.RESOLVED, resolvedAlert.getStatus());
        assertEquals("Investigated properly.", resolvedAlert.getNotes());
        assertFalse(resolvedAlert.isFalsePositive());
        assertNotNull(resolvedAlert.getResolutionTime());
    }
}
