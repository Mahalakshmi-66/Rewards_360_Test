
package com.rewards360.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String loyaltyTier;
    private int pointsBalance;
    private LocalDate nextExpiry;
    private String preferences; // CSV of categories
    private String communication; // Email/SMS/WhatsApp
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
