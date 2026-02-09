package com.rewards360.service.impl;

import com.rewards360.model.TransactionAnomaly;
import com.rewards360.repository.TransactionAnomalyRepository;
import com.rewards360.service.AnomalyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Consolidated Anomaly Service
 */
@Service
@Transactional
public class AnomalyServiceImpl implements AnomalyService {

    private final TransactionAnomalyRepository anomalyRepo;

    public AnomalyServiceImpl(TransactionAnomalyRepository anomalyRepo) {
        this.anomalyRepo = anomalyRepo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionAnomaly> findByTransactionId(String transactionId) {
        return anomalyRepo.findByTransactionIdOrderByDetectedAtDesc(transactionId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionAnomaly> findByAccountId(String accountId) {
        return anomalyRepo.findByAccountIdOrderByDetectedAtDesc(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionAnomaly> list(String severity, String type) {
        if (severity != null && !severity.isBlank()) {
            return anomalyRepo.findBySeverityOrderByDetectedAtDesc(severity.toUpperCase());
        }
        return anomalyRepo.findTop20ByOrderByDetectedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransactionAnomaly> get(Long id) {
        return anomalyRepo.findById(id);
    }
}
