package com.sms.contact_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Entity class mapped to contacts table in PostgreSQL
@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contact name cannot be empty
    @NotBlank(message = "Name is required")
    private String name;

    // Mobile number must be unique and 10 digits
    @Column(unique = true)
    @NotBlank(message = "Mobile number is required")
    @Pattern(
        regexp = "^[0-9]{10}$",
        message = "Mobile number must contain exactly 10 digits"
    )
    private String mobile;

    // Valid email format
    @Email(message = "Invalid email format")
    private String email;

    // Contact group
    @NotBlank(message = "Group name is required")
    private String groupName;
}