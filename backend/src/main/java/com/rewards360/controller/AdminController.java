
package com.rewards360.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rewards360.model.Transaction;
import com.rewards360.model.TransactionAnomaly;
import com.rewards360.model.AuditLog;
import com.rewards360.model.Campaign;
import com.rewards360.model.Alert;
import com.rewards360.model.Offer;
import com.rewards360.repository.TransactionRepository;
import com.rewards360.repository.TransactionAnomalyRepository;
import com.rewards360.repository.AuditLogRepository;
import com.rewards360.repository.CampaignRepository;
import com.rewards360.repository.AlertRepository;
import com.rewards360.repository.OfferRepository;
import com.rewards360.service.FraudDetectionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {
    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final AlertRepository fraudAlertRepository;
    private final TransactionAnomalyRepository anomalyRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    @PostMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign c){
        return ResponseEntity.ok(campaignRepository.save(c));
    }
    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Campaign> campaigns(){ return campaignRepository.findAll(); }

    @PostMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Offer> createOffer(@RequestBody Offer o){
        o.setActive(true);
        return ResponseEntity.ok(offerRepository.save(o));
    }
    @GetMapping("/offers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Offer> offers(){ return offerRepository.findAll(); }

    @GetMapping("/fraud/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Alert> alerts(){ return fraudAlertRepository.findAll(); }

    @GetMapping("/fraud/anomalies")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TransactionAnomaly> anomalies(){ return anomalyRepository.findAll(); }

    @GetMapping("/fraud/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> audit(){ return auditLogRepository.findAll(); }

    // Transaction Monitoring endpoints
    @GetMapping("/fraud/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Transaction> transactions(){ 
        return transactionRepository.findTop100ByOrderByCreatedAtDesc(); 
    }

    @PutMapping("/fraud/transactions/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transaction> reviewTransaction(@PathVariable Long id){
        return transactionRepository.findById(id)
            .map(tx -> {
                tx.setStatus("REVIEW");
                return ResponseEntity.ok(transactionRepository.save(tx));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/fraud/transactions/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Transaction> blockTransaction(@PathVariable Long id){
        return transactionRepository.findById(id)
            .map(tx -> {
                tx.setStatus("BLOCKED");
                return ResponseEntity.ok(transactionRepository.save(tx));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    // Fraud Detection endpoints
    @PostMapping("/fraud/analyze/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> analyzeTransaction(@PathVariable Long id){
        return transactionRepository.findById(id)
            .map(tx -> {
                fraudDetectionService.analyzeTransaction(tx);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Transaction analyzed",
                    "transactionId", tx.getTransactionId(),
                    "riskLevel", tx.getRiskLevel(),
                    "status", tx.getStatus()
                ));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/fraud/analyze-all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> analyzeAllTransactions(){
        List<Transaction> transactions = transactionRepository.findAll();
        int analyzed = 0;
        int blocked = 0;
        int reviewed = 0;
        int cleared = 0;

        for (Transaction tx : transactions) {
            fraudDetectionService.analyzeTransaction(tx);
            analyzed++;
            
            switch (tx.getStatus()) {
                case "BLOCKED": blocked++; break;
                case "REVIEW": reviewed++; break;
                case "CLEARED": cleared++; break;
            }
        }

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "All transactions analyzed",
            "totalAnalyzed", analyzed,
            "blocked", blocked,
            "flaggedForReview", reviewed,
            "cleared", cleared
        ));
    }
}
