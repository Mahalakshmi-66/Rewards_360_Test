
package com.rewards360.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "transaction_anomalies")
public class TransactionAnomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Business identifiers
    @Column(nullable = false)
    private String transactionId;

    private String accountId;

    // e.g., AMOUNT_SPIKE, VELOCITY, GEO_MISMATCH
    @Column(nullable = false)
    private String anomalyType;

    // 0.0 - 1.0 (or any scoring range you use)
    @Column(nullable = false)
    private double score;

    // LOW/MEDIUM/HIGH/CRITICAL
    @Column(nullable = false)
    private String severity;

    private String flaggedReason;

    @Column(nullable = false, updatable = false)
    private Instant detectedAt = Instant.now();

    // Optional linked alert id
    private Long alertId;

    // --- getters & setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getAccountId() { return accountId; }
    public void setAccountId(String accountId) { this.accountId = accountId; }

    public String getAnomalyType() { return anomalyType; }
    public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getFlaggedReason() { return flaggedReason; }
    public void setFlaggedReason(String flaggedReason) { this.flaggedReason = flaggedReason; }

    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
}
