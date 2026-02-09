package com.rewards360.service;

import com.rewards360.model.TransactionAnomaly;

import java.util.List;
import java.util.Optional;

/**
 * Consolidated Anomaly Service
 * Handles all anomaly operations
 */
public interface AnomalyService {
    List<TransactionAnomaly> findByTransactionId(String transactionId);
    List<TransactionAnomaly> findByAccountId(String accountId);
    List<TransactionAnomaly> list(String severity, String type);
    Optional<TransactionAnomaly> get(Long id);
}
