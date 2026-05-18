package com.bulksms.billing_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "billing_accounts")
@Data
public class BillingAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    private Integer credits;
}
