// src/main/java/com/rewards360/service/impl/AuditLogServiceImpl.java
package com.rewards360.service.impl;

import com.rewards360.model.AuditLog;
import com.rewards360.repository.AuditLogRepository;
import com.rewards360.service.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository repo;

    public AuditLogServiceImpl(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLog> list(String userId, String action, String entityType, Instant from, Instant to) {
        if (userId != null && !userId.isBlank()) {
            return repo.findByUserIdOrderByCreatedAtDesc(userId);
        }
        if (action != null && !action.isBlank()) {
            return repo.findByActionOrderByCreatedAtDesc(action.toUpperCase());
        }
        if (from != null && to != null) {
            return repo.findByCreatedAtBetweenOrderByCreatedAtDesc(from, to);
        }
        return repo.findTop200ByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuditLog> get(Long id) {
        return repo.findById(id);
    }

    @Override
    public AuditLog create(AuditLog log) {
        if (log.getAction() != null) log.setAction(log.getAction().toUpperCase());
        if (log.getEntityType() != null) log.setEntityType(log.getEntityType().toUpperCase());
        if (log.getCreatedAt() == null) log.setCreatedAt(Instant.now());
        return repo.save(log);
    }

    @Override
    public AuditLog recordSimple(String userId, String username, String action, String entityType, String entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setCreatedAt(Instant.now());
        return create(log);
    }
}