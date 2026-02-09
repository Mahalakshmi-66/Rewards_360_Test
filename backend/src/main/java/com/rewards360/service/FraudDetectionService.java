package com.rewards360.service;

import com.rewards360.model.Transaction;
import com.rewards360.model.TransactionAnomaly;
import com.rewards360.model.Alert;
import com.rewards360.repository.TransactionRepository;
import com.rewards360.repository.TransactionAnomalyRepository;
import com.rewards360.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final TransactionRepository transactionRepository;
    private final TransactionAnomalyRepository anomalyRepository;
    private final AlertRepository alertRepository;

    // Risk thresholds
    private static final BigDecimal LOW_AMOUNT_THRESHOLD = new BigDecimal("100");
    private static final BigDecimal MEDIUM_AMOUNT_THRESHOLD = new BigDecimal("1000");
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000");
    private static final BigDecimal CRITICAL_AMOUNT_THRESHOLD = new BigDecimal("50000");
    
    // Velocity thresholds
    private static final int VELOCITY_CHECK_MINUTES = 30;
    private static final int MAX_TRANSACTIONS_PER_30MIN = 5;
    
    // High-risk merchant categories
    private static final List<String> HIGH_RISK_CATEGORIES = List.of(
        "FINANCIAL", "CRYPTO", "LUXURY", "GAMBLING", "JEWELRY"
    );
    
    // High-risk locations (countries with high fraud rates)
    private static final List<String> HIGH_RISK_LOCATIONS = List.of(
        "Nigeria", "Russia", "China", "Vietnam", "Romania"
    );

    /**
     * Analyze a transaction and assign risk level, status, and create anomalies/alerts
     */
    @Transactional
    public void analyzeTransaction(Transaction transaction) {
        double riskScore = 0.0;
        List<String> reasons = new ArrayList<>();
        
        // 1. Amount-based risk scoring
        BigDecimal amount = transaction.getAmount();
        if (amount.compareTo(CRITICAL_AMOUNT_THRESHOLD) >= 0) {
            riskScore += 0.5;
            reasons.add("Transaction amount exceeds $50,000 (CRITICAL threshold)");
        } else if (amount.compareTo(HIGH_AMOUNT_THRESHOLD) >= 0) {
            riskScore += 0.3;
            reasons.add("Transaction amount exceeds $10,000 (HIGH threshold)");
        } else if (amount.compareTo(MEDIUM_AMOUNT_THRESHOLD) >= 0) {
            riskScore += 0.15;
            reasons.add("Transaction amount exceeds $1,000 (MEDIUM threshold)");
        }
        
        // 2. Merchant category risk
        String category = transaction.getMerchantCategory();
        if (category != null && HIGH_RISK_CATEGORIES.contains(category.toUpperCase())) {
            riskScore += 0.2;
            reasons.add("High-risk merchant category: " + category);
        }
        
        // 3. Location risk
        String location = transaction.getLocation();
        if (location != null) {
            for (String highRiskLoc : HIGH_RISK_LOCATIONS) {
                if (location.contains(highRiskLoc)) {
                    riskScore += 0.15;
                    reasons.add("Transaction from high-risk location: " + location);
                    break;
                }
            }
        }
        
        // 4. Velocity check - multiple transactions in short time
        if (transaction.getAccountId() != null) {
            Instant thirtyMinutesAgo = Instant.now().minus(VELOCITY_CHECK_MINUTES, ChronoUnit.MINUTES);
            List<Transaction> recentTransactions = transactionRepository
                .findByAccountIdAndCreatedAtAfter(transaction.getAccountId(), thirtyMinutesAgo);
            
            if (recentTransactions.size() >= MAX_TRANSACTIONS_PER_30MIN) {
                riskScore += 0.25;
                reasons.add("Velocity anomaly: " + recentTransactions.size() + " transactions in " + VELOCITY_CHECK_MINUTES + " minutes");
                
                // Create velocity anomaly
                createAnomaly(transaction, "VELOCITY", riskScore, 
                    "Multiple transactions detected: " + recentTransactions.size() + " in " + VELOCITY_CHECK_MINUTES + " minutes");
            }
        }
        
        // 5. Geographic anomaly - transaction from different location within short time
        if (transaction.getAccountId() != null && location != null) {
            Instant twoHoursAgo = Instant.now().minus(2, ChronoUnit.HOURS);
            List<Transaction> recentTxns = transactionRepository
                .findByAccountIdAndCreatedAtAfter(transaction.getAccountId(), twoHoursAgo);
            
            for (Transaction recentTx : recentTxns) {
                if (recentTx.getLocation() != null && !recentTx.getLocation().equals(location)) {
                    // Different location within 2 hours - possible geographic anomaly
                    String[] recentCountry = recentTx.getLocation().split(",");
                    String[] currentCountry = location.split(",");
                    
                    if (recentCountry.length > 1 && currentCountry.length > 1) {
                        String recentCountryCode = recentCountry[recentCountry.length - 1].trim();
                        String currentCountryCode = currentCountry[currentCountry.length - 1].trim();
                        
                        if (!recentCountryCode.equals(currentCountryCode)) {
                            riskScore += 0.2;
                            reasons.add("Geographic anomaly: Card used in different countries within 2 hours");
                            
                            // Create geographic anomaly
                            createAnomaly(transaction, "GEO_MISMATCH", riskScore,
                                "Card used in " + currentCountryCode + " after being used in " + recentCountryCode + " within 2 hours");
                            break;
                        }
                    }
                }
            }
        }
        
        // 6. Assign risk level based on final score
        String riskLevel;
        if (riskScore >= 0.7) {
            riskLevel = "CRITICAL";
        } else if (riskScore >= 0.4) {
            riskLevel = "HIGH";
        } else if (riskScore >= 0.2) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "LOW";
        }
        
        transaction.setRiskLevel(riskLevel);
        
        // 7. Assign status based on risk level
        if ("CRITICAL".equals(riskLevel)) {
            transaction.setStatus("BLOCKED");
            reasons.add("AUTO-BLOCKED due to CRITICAL risk level");
            
            // Create critical alert
            createAlert("CRITICAL", "AUTO-BLOCK: Critical fraud risk detected",
                "Transaction " + transaction.getTransactionId() + " automatically blocked. " + String.join(", ", reasons));
                
        } else if ("HIGH".equals(riskLevel)) {
            transaction.setStatus("REVIEW");
            reasons.add("AUTO-FLAGGED for review due to HIGH risk level");
            
            // Create high alert
            createAlert("HIGH", "AUTO-REVIEW: High fraud risk detected",
                "Transaction " + transaction.getTransactionId() + " flagged for review. " + String.join(", ", reasons));
                
        } else {
            transaction.setStatus("CLEARED");
        }
        
        // 8. Create anomaly if significant risk detected
        if (riskScore >= 0.3 && !reasons.isEmpty()) {
            createAnomaly(transaction, "AMOUNT_SPIKE", riskScore, String.join(", ", reasons));
        }
        
        // Save the transaction with updated risk and status
        transactionRepository.save(transaction);
    }
    
    /**
     * Create a transaction anomaly record
     */
    private void createAnomaly(Transaction transaction, String anomalyType, double score, String reason) {
        TransactionAnomaly anomaly = new TransactionAnomaly();
        anomaly.setTransactionId(transaction.getTransactionId());
        anomaly.setAccountId(transaction.getAccountId());
        anomaly.setAnomalyType(anomalyType);
        anomaly.setScore(Math.min(score, 1.0)); // Cap at 1.0
        
        // Determine severity based on score
        String severity;
        if (score >= 0.7) {
            severity = "CRITICAL";
        } else if (score >= 0.4) {
            severity = "HIGH";
        } else if (score >= 0.2) {
            severity = "MEDIUM";
        } else {
            severity = "LOW";
        }
        anomaly.setSeverity(severity);
        anomaly.setFlaggedReason(reason);
        anomaly.setDetectedAt(Instant.now());
        
        anomalyRepository.save(anomaly);
    }
    
    /**
     * Create a fraud alert
     */
    private void createAlert(String severity, String title, String description) {
        Alert alert = new Alert();
        alert.setSeverity(severity);
        alert.setStatus("OPEN");
        alert.setTitle(title);
        alert.setDescription(description);
        alert.setCreatedAt(Instant.now());
        
        alertRepository.save(alert);
    }
}
