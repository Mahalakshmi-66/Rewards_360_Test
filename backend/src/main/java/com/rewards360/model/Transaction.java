package com.rewards360.model;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "external_id", unique = true, nullable = false, length = 50)
    private String externalId;
    
    @Column(name = "type", nullable = false, length = 20)
    private String type; // PURCHASE, REDEMPTION, CLAIM
    
    @Column(name = "points_earned", nullable = false)
    private int pointsEarned;
    
    @Column(name = "points_redeemed", nullable = false)
    private int pointsRedeemed;
    
    @Column(name = "store", length = 100)
    private String store;
    
    @Column(name = "date", nullable = false)
    private LocalDate date;
    
    @Column(name = "expiry")
    private LocalDate expiry;
    
    @Column(name = "note", length = 500)
    private String note;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
