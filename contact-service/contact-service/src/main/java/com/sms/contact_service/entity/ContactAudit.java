package com.sms.contact_service.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "contact_audit")
@Data
public class ContactAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contact ID that was updated
    private Long contactId;

    // Old values
    private String oldName;
    private String oldMobile;
    private String oldEmail;
    private String oldGroupName;

    // New values
    private String newName;
    private String newMobile;
    private String newEmail;
    private String newGroupName;

    // When update happened
    private LocalDateTime updatedAt;
}
