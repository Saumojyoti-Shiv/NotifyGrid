package com.bulksms.contact_service.controller;

import com.bulksms.contact_service.dto.ContactGroupRequest;
import com.bulksms.contact_service.model.Contact;
import com.bulksms.contact_service.model.ContactGroup;
import com.bulksms.contact_service.repository.ContactGroupRepository;
import com.bulksms.contact_service.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactGroupRepository contactGroupRepository;

    @GetMapping
    public List<Contact> getAllContacts() {
        return contactRepository.findAll();
    }

    @PostMapping("/batch")
    public int saveContactsBatch(@RequestBody List<Contact> contacts) {
        int savedCount = 0;
        for (Contact contact : contacts) {
            Optional<Contact> existing = contactRepository.findByPhone(contact.getPhone());
            if (existing.isEmpty()) {
                contactRepository.save(contact);
                savedCount++;
            }
        }
        return savedCount;
    }

    @GetMapping("/groups")
    public List<ContactGroup> getAllGroups() {
        return contactGroupRepository.findAll();
    }

    @GetMapping("/groups/{id}")
    public Optional<ContactGroup> getGroupById(@PathVariable Long id) {
        return contactGroupRepository.findById(id);
    }

    @PostMapping("/groups")
    public ContactGroup createGroup(@RequestBody ContactGroupRequest request) {
        ContactGroup group = new ContactGroup();
        group.setName(request.getName());
        group.setFilterPrefix(request.getFilterPrefix());
        
        List<Contact> contacts = contactRepository.findByIdIn(request.getContactIds());
        group.setContacts(contacts);
        
        return contactGroupRepository.save(group);
    }

    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable Long id) {
        contactGroupRepository.deleteById(id);
    }
}
