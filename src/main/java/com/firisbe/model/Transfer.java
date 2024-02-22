package com.firisbe.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id")
    private Customer sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", referencedColumnName = "id")
    private Customer receiver;

    private BigDecimal amount;

    private LocalDateTime timestamp;
}
