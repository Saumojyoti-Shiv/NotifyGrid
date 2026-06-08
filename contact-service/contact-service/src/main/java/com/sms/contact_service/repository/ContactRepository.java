package com.sms.contact_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sms.contact_service.entity.Contact;

// Repository Layer
// Responsible for communicating with PostgreSQL database
// JpaRepository provides built-in CRUD operations
public interface ContactRepository extends JpaRepository<Contact, Long> {

    // Find contact using mobile number
    // Used for duplicate mobile validation
    Optional<Contact> findByMobile(String mobile);

    // Find all contacts belonging to a specific group
    
    List<Contact> findByGroupName(String groupName);

    // Search contacts whose name contains given text
    
    List<Contact> findByNameContaining(String name);
}