package com.bulksms.contact_service.repository;

import com.bulksms.contact_service.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByPhone(String phone);
    List<Contact> findByPhoneIn(List<String> phones);
    List<Contact> findByIdIn(List<Long> ids);
}
