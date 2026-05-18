package com.bulksms.billing_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_history")
@Data
public class BillingHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private Integer amount;
    private String type; // DEDUCTION, RECHARGE
    private String description;
    private LocalDateTime timestamp = LocalDateTime.now();
}
