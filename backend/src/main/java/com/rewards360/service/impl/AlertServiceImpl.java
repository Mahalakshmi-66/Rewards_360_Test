package com.rewards360.service.impl;

import com.rewards360.model.Alert;
import com.rewards360.model.Transaction;
import com.rewards360.model.TransactionAnomaly;
import com.rewards360.repository.AlertRepository;
import com.rewards360.service.AlertService;
import com.rewards360.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepo;
    private final AuditLogService audit;

    public AlertServiceImpl(AlertRepository alertRepo, AuditLogService audit) {
        this.alertRepo = alertRepo;
        this.audit = audit;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> list(String status, String severity) {
        if (status != null && !status.isBlank()) {
            return alertRepo.findByStatusOrderByCreatedAtDesc(status.toUpperCase());
        }
        if (severity != null && !severity.isBlank()) {
            return alertRepo.findBySeverityOrderByCreatedAtDesc(severity.toUpperCase());
        }
        return alertRepo.findAll().stream()
                .sorted(Comparator.comparing(Alert::getCreatedAt).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Alert> get(Long id) {
        return alertRepo.findById(id);
    }

    @Override
    public Alert create(Alert alert) {
        // normalize
        if (alert.getStatus() == null || alert.getStatus().isBlank()) alert.setStatus("OPEN");
        else alert.setStatus(alert.getStatus().toUpperCase());
        if (alert.getCreatedAt() == null) alert.setCreatedAt(Instant.now());
        if (alert.getSeverity() == null) alert.setSeverity("MEDIUM");
        else alert.setSeverity(alert.getSeverity().toUpperCase());
        alert.setUpdatedAt(null);

        Alert saved = alertRepo.save(alert);
        audit.recordSimple("SYSTEM", "system", "ALERT_CREATE",
                "ALERT", String.valueOf(saved.getId()), null);
        return saved;
    }

    @Override
    public Alert escalateFromAnomaly(TransactionAnomaly anomaly, Transaction tx) {
        Alert alert = new Alert();
        alert.setStatus("OPEN");
        alert.setTitle(switch (anomaly.getAnomalyType()) {
            case "AMOUNT_SPIKE" -> "High-value amount spike detected";
            case "VELOCITY"     -> "Velocity fraud detected";
            case "GEO_MISMATCH" -> "Geographic anomaly detected";
            default             -> "Anomaly detected";
        });
        alert.setDescription(
                "Anomaly: " + anomaly.getAnomalyType() +
                        ", score=" + anomaly.getScore() +
                        ", severity=" + anomaly.getSeverity() +
                        (anomaly.getFlaggedReason() != null ? ", reason=" + anomaly.getFlaggedReason() : "") +
                        (tx != null ? ("\nTransaction: " + tx.getTransactionId() +
                                ", amount=" + tx.getAmount() +
                                ", riskLevel=" + tx.getRiskLevel() +
                                ", location=" + tx.getLocation()) : "")
        );
        alert.setCreatedAt(Instant.now());
        
        // Use anomaly severity for alert
        alert.setSeverity(anomaly.getSeverity() != null ? anomaly.getSeverity() : "MEDIUM");

        Alert saved = alertRepo.save(alert);
        audit.recordSimple("SYSTEM", "system", "ALERT_CREATE",
                "ALERT", String.valueOf(saved.getId()), "fromAnomaly=" + anomaly.getId());
        return saved;
    }

    @Override
    public Optional<Alert> acknowledge(Long id, String userId, String username) {
        return alertRepo.findById(id).map(alert -> {
            alert.setStatus("ACKNOWLEDGED");
            alert.setAssignedTo(username);
            alert.setUpdatedAt(Instant.now());
            Alert saved = alertRepo.save(alert);
            audit.recordSimple(userId, username, "ALERT_ACKNOWLEDGE", "ALERT", String.valueOf(id), null);
            return saved;
        });
    }

    @Override
    public Optional<Alert> close(Long id, String userId, String username, String resolution) {
        return alertRepo.findById(id).map(alert -> {
            alert.setStatus("CLOSED");
            alert.setUpdatedAt(Instant.now());
            Alert saved = alertRepo.save(alert);
            audit.recordSimple(userId, username, "ALERT_CLOSE", "ALERT", String.valueOf(id), "resolution=" + resolution);
            return saved;
        });
    }
}