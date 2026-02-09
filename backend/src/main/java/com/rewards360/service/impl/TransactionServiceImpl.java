package com.rewards360.service.impl;

import com.rewards360.model.Alert;
import com.rewards360.model.Transaction;
import com.rewards360.model.TransactionAnomaly;
import com.rewards360.repository.TransactionAnomalyRepository;
import com.rewards360.repository.TransactionRepository;
import com.rewards360.service.TransactionService;
import com.rewards360.service.AlertService;
import com.rewards360.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Consolidated Transaction Service
 * Handles all transaction operations, fraud detection rules, and actions
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository txRepo;
    private final TransactionAnomalyRepository anomalyRepo;
    private final AlertService alertService;
    private final AuditLogService auditService;

    // Rule thresholds
    private static final BigDecimal AMOUNT_SPIKE_CRITICAL = BigDecimal.valueOf(3.0);
    private static final BigDecimal AMOUNT_SPIKE_HIGH = BigDecimal.valueOf(2.0);
    private static final BigDecimal AMOUNT_SPIKE_MEDIUM = BigDecimal.valueOf(1.5);
    private static final BigDecimal HIGH_VALUE_CRITICAL = BigDecimal.valueOf(100000);
    private static final BigDecimal HIGH_VALUE_HIGH = BigDecimal.valueOf(50000);
    private static final BigDecimal HIGH_VALUE_MEDIUM = BigDecimal.valueOf(20000);
    private static final long VELOCITY_CRITICAL = 10;
    private static final long VELOCITY_HIGH = 5;
    private static final long VELOCITY_MEDIUM = 3;
    private static final int VELOCITY_WINDOW_MINUTES = 10;
    private static final int ODD_HOUR_START = 23;
    private static final int ODD_HOUR_END = 6;
    private static final List<String> HIGH_RISK_CATEGORIES = Arrays.asList(
        "GAMBLING", "CRYPTOCURRENCY", "ADULT", "PHARMACEUTICALS", "MONEY_TRANSFER", "WIRE_TRANSFER", "GIFT_CARDS"
    );

    public TransactionServiceImpl(TransactionRepository txRepo,
                                 TransactionAnomalyRepository anomalyRepo,
                                 AlertService alertService,
                                 AuditLogService auditService) {
        this.txRepo = txRepo;
        this.anomalyRepo = anomalyRepo;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    // ==================== CRUD Operations ====================

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> list(String accountId, String riskLevel, String status, String paymentMethod,
                                  Instant from, Instant to, BigDecimal minAmount, BigDecimal maxAmount, String q) {
        String risk = riskLevel != null ? riskLevel.toUpperCase() : null;
        String st = status != null ? status.toUpperCase() : null;
        String pm = paymentMethod != null ? paymentMethod.toUpperCase() : null;

        if (allNull(accountId, risk, st, pm, from, to, minAmount, maxAmount, q)) {
            return txRepo.findTop100ByOrderByCreatedAtDesc();
        }
        return txRepo.findFiltered(accountId, risk, st, pm, from, to, minAmount, maxAmount, 
                (q == null || q.isBlank()) ? null : q);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Transaction> get(Long id) {
        return txRepo.findById(id);
    }

    @Override
    public Transaction create(Transaction tx) {
        // Normalize
        if (tx.getStatus() == null || tx.getStatus().isBlank()) tx.setStatus("CLEARED");
        else tx.setStatus(tx.getStatus().toUpperCase());
        if (tx.getRiskLevel() != null) tx.setRiskLevel(tx.getRiskLevel().toUpperCase());
        if (tx.getCurrency() != null) tx.setCurrency(tx.getCurrency().toUpperCase());
        if (tx.getPaymentMethod() != null) tx.setPaymentMethod(tx.getPaymentMethod().toUpperCase());
        if (tx.getCreatedAt() == null) tx.setCreatedAt(Instant.now());
        tx.setUpdatedAt(null);

        Transaction saved = txRepo.save(tx);
        auditService.recordSimple("SYSTEM", "system", "TX_CREATE", "TRANSACTION", saved.getTransactionId(),
                "amount=" + saved.getAmount() + ", merchant=" + saved.getMerchantName());

        // Run fraud detection rules
        List<TransactionAnomaly> anomalies = evaluateRules(saved);

        // Update risk level based on anomalies
        if (!anomalies.isEmpty()) {
            updateRiskLevel(saved);
            saved = txRepo.findById(saved.getId()).orElse(saved);
        }

        // Create alerts for HIGH/CRITICAL anomalies
        for (TransactionAnomaly anomaly : anomalies) {
            if ("HIGH".equals(anomaly.getSeverity()) || "CRITICAL".equals(anomaly.getSeverity())) {
                Alert alert = alertService.escalateFromAnomaly(anomaly, saved);
                anomaly.setAlertId(alert.getId());
                auditService.recordSimple("SYSTEM", "system", "ALERT_FROM_ANOMALY", "ALERT", String.valueOf(alert.getId()),
                        "anomalyType=" + anomaly.getAnomalyType() + ", txId=" + saved.getTransactionId());
            }
        }

        // Auto-mark for review if CRITICAL
        if ("CRITICAL".equals(saved.getRiskLevel()) && "CLEARED".equals(saved.getStatus())) {
            saved.setStatus("REVIEW");
            saved.setUpdatedAt(Instant.now());
            saved = txRepo.save(saved);
            auditService.recordSimple("SYSTEM", "system", "TX_AUTO_REVIEW", "TRANSACTION", saved.getTransactionId(),
                    "Automatically marked for review due to CRITICAL risk level");
        }

        return saved;
    }

    @Override
    public Optional<Transaction> updateStatus(Long id, String newStatus) {
        return txRepo.findById(id).map(tx -> {
            String oldStatus = tx.getStatus();
            tx.setStatus(newStatus.toUpperCase());
            tx.setUpdatedAt(Instant.now());
            Transaction saved = txRepo.save(tx);
            auditService.recordSimple("SYSTEM", "system", "TX_STATUS_UPDATE", "TRANSACTION", tx.getTransactionId(),
                    "Status: " + oldStatus + " -> " + newStatus);
            return saved;
        });
    }

    // ==================== Action Operations ====================

    @Override
    public Transaction markForReview(Long transactionId, String userId, String username, String reason) {
        Transaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        String previousStatus = tx.getStatus();
        tx.setStatus("REVIEW");
        tx.setUpdatedAt(Instant.now());
        Transaction saved = txRepo.save(tx);
        auditService.recordSimple(userId, username, "TX_REVIEW", "TRANSACTION", tx.getTransactionId(),
                String.format("Status changed: %s -> REVIEW. Reason: %s", previousStatus, reason != null ? reason : "Manual review"));
        return saved;
    }

    @Override
    public Transaction blockTransaction(Long transactionId, String userId, String username, String reason) {
        Transaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        String previousStatus = tx.getStatus();
        tx.setStatus("BLOCKED");
        tx.setUpdatedAt(Instant.now());
        if (tx.getRiskLevel() == null || !tx.getRiskLevel().equals("CRITICAL")) {
            tx.setRiskLevel("CRITICAL");
        }
        Transaction saved = txRepo.save(tx);
        auditService.recordSimple(userId, username, "TX_BLOCK", "TRANSACTION", tx.getTransactionId(),
                String.format("Status changed: %s -> BLOCKED. Reason: %s. Risk Level: %s", 
                        previousStatus, reason != null ? reason : "Fraud detected", tx.getRiskLevel()));
        return saved;
    }

    @Override
    public Transaction clearTransaction(Long transactionId, String userId, String username, String reason) {
        Transaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        String previousStatus = tx.getStatus();
        tx.setStatus("CLEARED");
        tx.setUpdatedAt(Instant.now());
        if ("HIGH".equals(tx.getRiskLevel()) || "CRITICAL".equals(tx.getRiskLevel())) {
            tx.setRiskLevel("MEDIUM");
        }
        Transaction saved = txRepo.save(tx);
        auditService.recordSimple(userId, username, "TX_CLEAR", "TRANSACTION", tx.getTransactionId(),
                String.format("Status changed: %s -> CLEARED. Reason: %s", previousStatus, reason != null ? reason : "Verified as legitimate"));
        return saved;
    }

    @Override
    public void bulkMarkForReview(Long[] transactionIds, String userId, String username, String reason) {
        for (Long txId : transactionIds) {
            try {
                markForReview(txId, userId, username, reason);
            } catch (Exception e) {
                auditService.recordSimple(userId, username, "TX_REVIEW_FAILED", "TRANSACTION", String.valueOf(txId), 
                        "Failed to mark for review: " + e.getMessage());
            }
        }
        auditService.recordSimple(userId, username, "TX_BULK_REVIEW", "TRANSACTION", "BULK", 
                String.format("Marked %d transactions for review. Reason: %s", transactionIds.length, reason));
    }

    @Override
    public void bulkBlockTransactions(Long[] transactionIds, String userId, String username, String reason) {
        for (Long txId : transactionIds) {
            try {
                blockTransaction(txId, userId, username, reason);
            } catch (Exception e) {
                auditService.recordSimple(userId, username, "TX_BLOCK_FAILED", "TRANSACTION", String.valueOf(txId), 
                        "Failed to block: " + e.getMessage());
            }
        }
        auditService.recordSimple(userId, username, "TX_BULK_BLOCK", "TRANSACTION", "BULK", 
                String.format("Blocked %d transactions. Reason: %s", transactionIds.length, reason));
    }

    // ==================== Fraud Detection Rules ====================

    @Override
    public List<TransactionAnomaly> processTransaction(Long transactionId) {
        Transaction tx = txRepo.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));
        List<TransactionAnomaly> anomalies = evaluateRules(tx);
        if (!anomalies.isEmpty()) {
            updateRiskLevel(tx);
        }
        for (TransactionAnomaly anomaly : anomalies) {
            if ("HIGH".equals(anomaly.getSeverity()) || "CRITICAL".equals(anomaly.getSeverity())) {
                Alert alert = alertService.escalateFromAnomaly(anomaly, tx);
                anomaly.setAlertId(alert.getId());
            }
        }
        auditService.recordSimple("SYSTEM", "system", "TX_REPROCESS", "TRANSACTION", tx.getTransactionId(),
                "Reprocessed, found " + anomalies.size() + " anomalies");
        return anomalies;
    }

    @Override
    public void reprocessAllTransactions() {
        List<Transaction> allTx = txRepo.findAll();
        int processed = 0, anomaliesFound = 0;
        for (Transaction tx : allTx) {
            try {
                List<TransactionAnomaly> anomalies = processTransaction(tx.getId());
                processed++;
                anomaliesFound += anomalies.size();
            } catch (Exception e) {
                auditService.recordSimple("SYSTEM", "system", "TX_REPROCESS_ERROR", "TRANSACTION", tx.getTransactionId(),
                        "Error: " + e.getMessage());
            }
        }
        auditService.recordSimple("SYSTEM", "system", "TX_BULK_REPROCESS", "TRANSACTION", "BULK",
                String.format("Processed %d transactions, found %d anomalies", processed, anomaliesFound));
    }

    private List<TransactionAnomaly> evaluateRules(Transaction tx) {
        List<TransactionAnomaly> anomalies = new ArrayList<>();

        // Rule 1: Amount Spike
        BigDecimal avgAmount = calculateAverage(tx.getAccountId());
        if (avgAmount != null && tx.getAmount().compareTo(avgAmount.multiply(AMOUNT_SPIKE_MEDIUM)) >= 0) {
            anomalies.addAll(detectAmountSpike(tx, avgAmount));
        }

        // Rule 2: Velocity
        long count = countRecent(tx.getAccountId());
        if (count >= VELOCITY_MEDIUM) {
            anomalies.add(createAnomaly(tx, "VELOCITY", getVelocityScore(count), 
                    count + " transactions in " + VELOCITY_WINDOW_MINUTES + " minutes", getVelocitySeverity(count)));
        }

        // Rule 3: Geographic Anomaly
        String prevLoc = getPreviousLocation(tx.getAccountId(), tx.getId());
        if (prevLoc != null && tx.getLocation() != null && !tx.getLocation().equalsIgnoreCase(prevLoc) && isDifferentCountry(tx.getLocation(), prevLoc)) {
            anomalies.add(createAnomaly(tx, "GEO_MISMATCH", 0.70, "Location changed from " + prevLoc + " to " + tx.getLocation(), "HIGH"));
        }

        // Rule 4: Timing
        int hour = tx.getCreatedAt().atZone(ZoneId.systemDefault()).getHour();
        if (hour >= ODD_HOUR_START || hour < ODD_HOUR_END) {
            anomalies.add(createAnomaly(tx, "ODD_HOURS", 0.40, "Transaction at unusual hour: " + hour + ":00", "MEDIUM"));
        }

        // Rule 5: High-Risk Merchant
        if (tx.getMerchantCategory() != null && HIGH_RISK_CATEGORIES.stream().anyMatch(cat -> tx.getMerchantCategory().toUpperCase().contains(cat))) {
            anomalies.add(createAnomaly(tx, "HIGH_RISK_MERCHANT", 0.65, "High-risk merchant category: " + tx.getMerchantCategory(), "HIGH"));
        }

        // Rule 6: High-Value
        if (tx.getAmount().compareTo(HIGH_VALUE_MEDIUM) >= 0) {
            anomalies.addAll(detectHighValue(tx));
        }

        // Save anomalies
        anomalies.forEach(anomaly -> {
            TransactionAnomaly saved = anomalyRepo.save(anomaly);
            auditService.recordSimple("SYSTEM", "system", "ANOMALY_DETECT", "TRANSACTION", tx.getTransactionId(),
                    "type=" + saved.getAnomalyType() + ", severity=" + saved.getSeverity());
        });

        return anomalies;
    }

    private List<TransactionAnomaly> detectAmountSpike(Transaction tx, BigDecimal avgAmount) {
        List<TransactionAnomaly> result = new ArrayList<>();
        BigDecimal ratio = tx.getAmount().divide(avgAmount, 2, RoundingMode.HALF_UP);
        if (tx.getAmount().compareTo(avgAmount.multiply(AMOUNT_SPIKE_CRITICAL)) >= 0) {
            result.add(createAnomaly(tx, "AMOUNT_SPIKE", 0.90, "Amount " + tx.getAmount() + " is " + ratio + "x average " + avgAmount, "CRITICAL"));
        } else if (tx.getAmount().compareTo(avgAmount.multiply(AMOUNT_SPIKE_HIGH)) >= 0) {
            result.add(createAnomaly(tx, "AMOUNT_SPIKE", 0.70, "Amount " + tx.getAmount() + " is " + ratio + "x average " + avgAmount, "HIGH"));
        } else {
            result.add(createAnomaly(tx, "AMOUNT_SPIKE", 0.50, "Amount " + tx.getAmount() + " is " + ratio + "x average " + avgAmount, "MEDIUM"));
        }
        return result;
    }

    private List<TransactionAnomaly> detectHighValue(Transaction tx) {
        List<TransactionAnomaly> result = new ArrayList<>();
        if (tx.getAmount().compareTo(HIGH_VALUE_CRITICAL) >= 0) {
            result.add(createAnomaly(tx, "HIGH_VALUE", 0.85, "High-value transaction: " + tx.getAmount() + " " + tx.getCurrency(), "CRITICAL"));
        } else if (tx.getAmount().compareTo(HIGH_VALUE_HIGH) >= 0) {
            result.add(createAnomaly(tx, "HIGH_VALUE", 0.65, "High-value transaction: " + tx.getAmount() + " " + tx.getCurrency(), "HIGH"));
        } else {
            result.add(createAnomaly(tx, "HIGH_VALUE", 0.45, "High-value transaction: " + tx.getAmount() + " " + tx.getCurrency(), "MEDIUM"));
        }
        return result;
    }

    private void updateRiskLevel(Transaction tx) {
        List<TransactionAnomaly> anomalies = anomalyRepo.findByTransactionIdOrderByDetectedAtDesc(tx.getTransactionId());
        if (anomalies.isEmpty()) {
            tx.setRiskLevel("LOW");
            return;
        }
        boolean hasCritical = anomalies.stream().anyMatch(a -> "CRITICAL".equals(a.getSeverity()));
        boolean hasHigh = anomalies.stream().anyMatch(a -> "HIGH".equals(a.getSeverity()));
        int count = anomalies.size();
        if (hasCritical || count >= 4) tx.setRiskLevel("CRITICAL");
        else if (hasHigh || count >= 3) tx.setRiskLevel("HIGH");
        else if (count >= 2) tx.setRiskLevel("MEDIUM");
        else tx.setRiskLevel("LOW");
        tx.setUpdatedAt(Instant.now());
        txRepo.save(tx);
    }

    private TransactionAnomaly createAnomaly(Transaction tx, String type, double score, String reason, String severity) {
        TransactionAnomaly anomaly = new TransactionAnomaly();
        anomaly.setTransactionId(tx.getTransactionId());
        anomaly.setAccountId(tx.getAccountId());
        anomaly.setAnomalyType(type);
        anomaly.setScore(score);
        anomaly.setSeverity(severity);
        anomaly.setFlaggedReason(reason);
        anomaly.setDetectedAt(Instant.now());
        return anomaly;
    }

    private BigDecimal calculateAverage(String accountId) {
        List<Transaction> recent = txRepo.findTop10ByAccountIdOrderByCreatedAtDesc(accountId);
        if (recent == null || recent.isEmpty()) return null;
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;
        for (Transaction t : recent) {
            if (t.getAmount() != null) {
                sum = sum.add(t.getAmount());
                count++;
            }
        }
        return count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : null;
    }

    private long countRecent(String accountId) {
        Instant to = Instant.now();
        Instant from = to.minus(VELOCITY_WINDOW_MINUTES, ChronoUnit.MINUTES);
        return txRepo.countByAccountIdAndCreatedAtBetween(accountId, from, to);
    }

    private String getPreviousLocation(String accountId, Long currentId) {
        List<Transaction> recent = txRepo.findTop10ByAccountIdOrderByCreatedAtDesc(accountId);
        return recent.stream()
                .filter(t -> t.getId() != null && !t.getId().equals(currentId))
                .map(Transaction::getLocation)
                .filter(loc -> loc != null && !loc.isBlank())
                .findFirst().orElse(null);
    }

    private boolean isDifferentCountry(String loc1, String loc2) {
        if (loc1 == null || loc2 == null) return false;
        String[] p1 = loc1.split(","), p2 = loc2.split(",");
        if (p1.length < 2 || p2.length < 2) return false;
        return !p1[p1.length - 1].trim().equalsIgnoreCase(p2[p2.length - 1].trim());
    }

    private double getVelocityScore(long count) {
        if (count >= VELOCITY_CRITICAL) return 0.95;
        if (count >= VELOCITY_HIGH) return 0.75;
        return 0.50;
    }

    private String getVelocitySeverity(long count) {
        if (count >= VELOCITY_CRITICAL) return "CRITICAL";
        if (count >= VELOCITY_HIGH) return "HIGH";
        return "MEDIUM";
    }

    private boolean allNull(Object... vals) {
        for (Object v : vals) if (v != null) return false;
        return true;
    }
}
