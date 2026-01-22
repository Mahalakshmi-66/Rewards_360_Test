
package com.rewards360.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Redemption {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String confirmationCode;
    private String transactionId;
    private LocalDate date;
    private int costPoints;
    private String offerTitle;
    private String store;
    @ManyToOne
    private User user;
}
