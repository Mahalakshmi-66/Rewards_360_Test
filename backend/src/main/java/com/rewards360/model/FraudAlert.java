
package com.rewards360.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FraudAlert {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String severity; // HIGH, MEDIUM, LOW
    private String message;
    private String refTxnId;
}
