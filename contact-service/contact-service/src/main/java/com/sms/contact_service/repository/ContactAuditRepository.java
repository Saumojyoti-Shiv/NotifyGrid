package com.sms.contact_service.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sms.contact_service.entity.ContactAudit;

public interface ContactAuditRepository
        extends JpaRepository<ContactAudit, Long> {

    // Returns audit history of a particular contact
    List<ContactAudit> findByContactId(Long contactId);
}
