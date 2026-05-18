package com.bulksms.contact_service.repository;

import com.bulksms.contact_service.model.ContactGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactGroupRepository extends JpaRepository<ContactGroup, Long> {
}
