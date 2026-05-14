package com.frauddetector.service;

import com.frauddetector.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure unit tests for FraudScoringService.
 * No mocks needed — the service is now DB-free and accepts a FraudContext POJO.
 */
class FraudScoringServiceTest {

    private FraudScoringService service;

    // Helper to create a baseline transaction with a given amount and timestamp
    private Transaction makeTransaction(BigDecimal amount, LocalDateTime timestamp, boolean international) {
        Transaction tx = new Transaction();
        tx.setAmount(amount);
        tx.setCreatedAt(timestamp);
        tx.setInternational(international);
        return tx;
    }

    // Helper for a daytime domestic transaction
    private Transaction daytimeTx(BigDecimal amount) {
        return makeTransaction(amount, LocalDateTime.now().withHour(10), false);
    }

    // Baseline context — zero risk on all auxiliary factors
    private FraudContext baseContext() {
        FraudContext ctx = new FraudContext();
        ctx.setRecentTransactionCount(0);
        ctx.setNewDevice(false);
        ctx.setBlacklisted(false);
        ctx.setHasRecentFailedLogins(false);
        return ctx;
    }

    @BeforeEach
    void setUp() {
        service = new FraudScoringService();
    }

    // ─── Amount Rules ────────────────────────────────────────────────────────

    @Test
    @DisplayName("High amount (>₹1L, ≤₹5L) adds exactly 30 points")
    void testHighAmountRule_addsCorrectScore() {
        Transaction tx = daytimeTx(new BigDecimal("150000"));
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(30, score, "Expected +30 for amount between 1L and 5L");
    }

    @Test
    @DisplayName("Very high amount (>₹5L) adds exactly 50 points, NOT 80")
    void testVeryHighAmountRule_doesNotStackWithHighAmount() {
        Transaction tx = daytimeTx(new BigDecimal("600000"));
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(50, score, "Scores must be mutually exclusive — only +50 for >5L");
    }

    @Test
    @DisplayName("Normal amount (≤₹1L) adds zero amount-related points")
    void testNormalAmount_addsZeroAmountScore() {
        Transaction tx = daytimeTx(new BigDecimal("50000"));
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(0, score, "No amount score for ≤1L");
    }

    // ─── Night Transaction Rules ─────────────────────────────────────────────

    @Test
    @DisplayName("Transaction at 2 AM triggers night rule (+15)")
    void testNightTransactionRule_triggersCorrectHours() {
        Transaction tx = makeTransaction(new BigDecimal("1000"), LocalDateTime.now().withHour(2), false);
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(15, score, "Night transaction (hour 1-5) should add +15");
    }

    @Test
    @DisplayName("Transaction at 5 AM (boundary) triggers night rule (+15)")
    void testNightTransactionRule_triggersAtBoundary() {
        Transaction tx = makeTransaction(new BigDecimal("1000"), LocalDateTime.now().withHour(5), false);
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(15, score, "Hour 5 is within night window");
    }

    @Test
    @DisplayName("Transaction at 10 AM does not trigger night rule")
    void testNightTransactionRule_doesNotTriggerDaytime() {
        Transaction tx = makeTransaction(new BigDecimal("1000"), LocalDateTime.now().withHour(10), false);
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(0, score, "Daytime transaction should not trigger night rule");
    }

    @Test
    @DisplayName("Transaction at midnight (hour 0) does not trigger night rule")
    void testNightTransactionRule_doesNotTriggerMidnight() {
        Transaction tx = makeTransaction(new BigDecimal("1000"), LocalDateTime.now().withHour(0), false);
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(0, score, "Midnight (hour 0) is outside the 1-5 window");
    }

    // ─── Velocity Breach ─────────────────────────────────────────────────────

    @Test
    @DisplayName("5+ recent transactions triggers velocity breach (+25)")
    void testVelocityBreachRule_triggersAboveThreshold() {
        Transaction tx = daytimeTx(new BigDecimal("1000"));
        FraudContext ctx = baseContext();
        ctx.setRecentTransactionCount(5);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(25, score, "Velocity breach at count >= 5 should add +25");
    }

    @Test
    @DisplayName("4 recent transactions does not trigger velocity breach")
    void testVelocityBreachRule_doesNotTriggerBelowThreshold() {
        Transaction tx = daytimeTx(new BigDecimal("1000"));
        FraudContext ctx = baseContext();
        ctx.setRecentTransactionCount(4);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(0, score, "Velocity breach should NOT fire at count < 5");
    }

    // ─── Blacklisted Receiver ────────────────────────────────────────────────

    @Test
    @DisplayName("Blacklisted receiver adds +40 points")
    void testBlacklistedReceiverRule_addsCorrectScore() {
        Transaction tx = daytimeTx(new BigDecimal("1000"));
        FraudContext ctx = baseContext();
        ctx.setBlacklisted(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(40, score, "Blacklisted receiver should add +40");
    }

    // ─── New Device ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("New device adds +20 points")
    void testNewDeviceRule_addsCorrectScore() {
        Transaction tx = daytimeTx(new BigDecimal("1000"));
        FraudContext ctx = baseContext();
        ctx.setNewDevice(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(20, score, "New device should add +20");
    }

    // ─── International Transfer ──────────────────────────────────────────────

    @Test
    @DisplayName("International transfer adds +20 points")
    void testInternationalTransferRule_addsCorrectScore() {
        Transaction tx = makeTransaction(new BigDecimal("1000"), LocalDateTime.now().withHour(10), true);
        int score = service.calculateRiskScore(tx, baseContext());
        assertEquals(20, score, "International transfer should add +20");
    }

    // ─── Failed Logins ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Recent failed logins flag adds +15 points")
    void testFailedLoginRule_addsCorrectScore() {
        Transaction tx = daytimeTx(new BigDecimal("1000"));
        FraudContext ctx = baseContext();
        ctx.setHasRecentFailedLogins(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(15, score, "Recent failed logins should add +15");
    }

    // ─── Combined Scenarios ──────────────────────────────────────────────────

    @Test
    @DisplayName("Multiple low-weight rules accumulate correctly")
    void testMultipleRules_scoresAddCorrectly() {
        // International (+20) + Night (+15) + NewDevice (+20) = 55
        Transaction tx = makeTransaction(new BigDecimal("5000"), LocalDateTime.now().withHour(3), true);
        FraudContext ctx = baseContext();
        ctx.setNewDevice(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(55, score, "International + Night + NewDevice should sum to 55");
    }

    @Test
    @DisplayName("Low risk transaction stays well below alert threshold of 21")
    void testLowRiskTransaction_noAlertThreshold() {
        Transaction tx = daytimeTx(new BigDecimal("500"));
        int score = service.calculateRiskScore(tx, baseContext());
        assertTrue(score < 21, "Score should be below the alert threshold of 21 for clean transactions");
    }

    @Test
    @DisplayName("Critical threshold scenario: very high amount + blacklisted + night = 105 (CRITICAL)")
    void testCriticalThreshold_correctSeverityAssigned() {
        // >500k (+50) + Blacklisted (+40) + Night (+15) = 105 → CRITICAL (>80)
        Transaction tx = makeTransaction(new BigDecimal("600000"), LocalDateTime.now().withHour(2), false);
        FraudContext ctx = baseContext();
        ctx.setBlacklisted(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertTrue(score >= 81, "Score of " + score + " should map to CRITICAL severity (≥81)");
    }

    @Test
    @DisplayName("All rules firing simultaneously produces correct cumulative score")
    void testAllRules_maximumScore() {
        // >500k (+50) + Velocity (+25) + Night (+15) + NewDevice (+20) + Blacklisted (+40) + International (+20) + FailedLogins (+15) = 185
        Transaction tx = makeTransaction(new BigDecimal("600000"), LocalDateTime.now().withHour(3), true);
        FraudContext ctx = new FraudContext();
        ctx.setRecentTransactionCount(5);
        ctx.setNewDevice(true);
        ctx.setBlacklisted(true);
        ctx.setHasRecentFailedLogins(true);
        int score = service.calculateRiskScore(tx, ctx);
        assertEquals(185, score, "All rules firing should produce 185 total score");
    }
}
