package com.sms.contact_service.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sms.contact_service.entity.Contact;
import com.sms.contact_service.entity.ContactAudit;
import com.sms.contact_service.exception.ResourceNotFoundException;
import com.sms.contact_service.repository.ContactAuditRepository;
import com.sms.contact_service.repository.ContactRepository;

@Service
public class ContactService {

    private static final Logger logger =
            LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository repository;
    private final ContactAuditRepository auditRepository;

    public ContactService(
            ContactRepository repository,
            ContactAuditRepository auditRepository) {

        this.repository = repository;
        this.auditRepository = auditRepository;
    }

    // ==========================
    // CREATE CONTACT
    // ==========================
    public Contact addContact(Contact contact) {

        logger.info("Attempting to add contact: {}", contact.getMobile());

        if (repository.findByMobile(contact.getMobile()).isPresent()) {

            logger.error("Duplicate mobile number found: {}",
                    contact.getMobile());

            throw new RuntimeException("Mobile number already exists");
        }

        Contact savedContact = repository.save(contact);

        logger.info("Contact added successfully with ID: {}",
                savedContact.getId());

        return savedContact;
    }

    // ==========================
    // GET ALL CONTACTS
    // ==========================
    public List<Contact> getAllContacts() {

        logger.info("Fetching all contacts");

        return repository.findAll();
    }
    // ==========================
    // GET CONTACT BY ID
    // ==========================
public Contact getContactById(Long id) {

    logger.info("Fetching contact with ID: {}", id);

    return repository.findById(id)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Contact not found with id " + id));
}

    // ==========================
    // UPDATE CONTACT + AUDIT
    // ==========================
    public Contact updateContact(Long id, Contact updatedContact) {

        logger.info("Updating contact with ID: {}", id);

        Contact contact = repository.findById(id)
                .orElseThrow(() -> {

                    logger.error("Contact not found with ID: {}", id);

                    return new ResourceNotFoundException(
                            "Contact not found with id " + id);
                });

        // Save Audit History
        ContactAudit audit = new ContactAudit();

        audit.setContactId(contact.getId());

        audit.setOldName(contact.getName());
        audit.setOldMobile(contact.getMobile());
        audit.setOldEmail(contact.getEmail());
        audit.setOldGroupName(contact.getGroupName());

        audit.setNewName(updatedContact.getName());
        audit.setNewMobile(updatedContact.getMobile());
        audit.setNewEmail(updatedContact.getEmail());
        audit.setNewGroupName(updatedContact.getGroupName());

        audit.setUpdatedAt(LocalDateTime.now());

        auditRepository.save(audit);

        // Update Contact
        contact.setName(updatedContact.getName());
        contact.setEmail(updatedContact.getEmail());
        contact.setMobile(updatedContact.getMobile());
        contact.setGroupName(updatedContact.getGroupName());

        Contact savedContact = repository.save(contact);

        logger.info("Contact updated successfully: {}", id);

        return savedContact;
    }

    // ==========================
    // DELETE CONTACT
    // ==========================
    public void deleteContact(Long id) {

        logger.info("Deleting contact with ID: {}", id);

        if (!repository.existsById(id)) {

            logger.error("Delete failed. Contact not found: {}", id);

            throw new ResourceNotFoundException(
                    "Contact not found with id " + id);
        }

        repository.deleteById(id);

        logger.info("Contact deleted successfully: {}", id);
    }

    // ==========================
    // SEARCH BY NAME
    // ==========================
    public List<Contact> searchByName(String name) {

        logger.info("Searching contact by name: {}", name);

        return repository.findByNameContaining(name);
    }

    // ==========================
    // SEARCH BY GROUP
    // ==========================
    public List<Contact> searchByGroup(String groupName) {

        logger.info("Searching contacts in group: {}", groupName);

        return repository.findByGroupName(groupName);
    }

    // ==========================
    // SEARCH BY MOBILE
    // ==========================
    public Contact searchByMobile(String mobile) {

        logger.info("Searching contact by mobile: {}", mobile);

        return repository.findByMobile(mobile)
                .orElseThrow(() -> {

                    logger.error("Contact not found: {}", mobile);

                    return new ResourceNotFoundException(
                            "Contact not found with mobile: " + mobile);
                });
    }

    // ==========================
    // TOTAL CONTACT COUNT
    // ==========================
    public long countContacts() {

        logger.info("Counting total contacts");

        return repository.count();
    }

    // ==========================
    // GET AUDIT HISTORY
    // ==========================
    public List<ContactAudit> getAuditHistory(Long contactId) {

        logger.info("Fetching audit history for contact {}",
                contactId);

        return auditRepository.findByContactId(contactId);
    }
}