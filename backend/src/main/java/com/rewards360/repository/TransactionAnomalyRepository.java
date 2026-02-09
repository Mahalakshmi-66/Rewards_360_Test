
package com.rewards360.repository;

import com.rewards360.model.TransactionAnomaly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionAnomalyRepository extends JpaRepository<TransactionAnomaly, Long> {
    List<TransactionAnomaly> findTop20ByOrderByDetectedAtDesc();
    List<TransactionAnomaly> findBySeverityOrderByDetectedAtDesc(String severity);
    List<TransactionAnomaly> findByTransactionIdOrderByDetectedAtDesc(String transactionId);
    List<TransactionAnomaly> findByAccountIdOrderByDetectedAtDesc(String accountId);
}

