
package com.rewards360.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String externalId;
    private String type; // PURCHASE, REDEMPTION, CLAIM
    private int pointsEarned;
    private int pointsRedeemed;
    private String store;
    private LocalDate date;
    private LocalDate expiry;
    private String note;
    @ManyToOne
    private User user;
}
