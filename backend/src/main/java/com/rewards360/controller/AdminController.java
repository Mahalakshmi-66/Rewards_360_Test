
package com.rewards360.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rewards360.model.Anomaly;
import com.rewards360.model.AuditLog;
import com.rewards360.model.Campaign;
import com.rewards360.model.FraudAlert;
import com.rewards360.model.Offer;
import com.rewards360.repository.AnomalyRepository;
import com.rewards360.repository.AuditLogRepository;
import com.rewards360.repository.CampaignRepository;
import com.rewards360.repository.FraudAlertRepository;
import com.rewards360.repository.OfferRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin
public class AdminController {
    private final CampaignRepository campaignRepository;
    private final OfferRepository offerRepository;
    private final FraudAlertRepository fraudAlertRepository;
    private final AnomalyRepository anomalyRepository;
    private final AuditLogRepository auditLogRepository;

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
    public List<FraudAlert> alerts(){ return fraudAlertRepository.findAll(); }

    @GetMapping("/fraud/anomalies")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Anomaly> anomalies(){ return anomalyRepository.findAll(); }

    @GetMapping("/fraud/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AuditLog> audit(){ return auditLogRepository.findAll(); }
}
