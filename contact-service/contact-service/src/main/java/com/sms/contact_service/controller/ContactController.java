package com.sms.contact_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sms.contact_service.entity.Contact;
import com.sms.contact_service.entity.ContactAudit;
import com.sms.contact_service.service.CSVService;
import com.sms.contact_service.service.ContactService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/contacts")
public class ContactController {

    private final ContactService service;
    private final CSVService csvService;

    public ContactController(
            ContactService service,
            CSVService csvService) {

        this.service = service;
        this.csvService = csvService;
    }

    @PostMapping
    public Contact addContact(
            @Valid @RequestBody Contact contact) {

        return service.addContact(contact);
    }

    @GetMapping
    public List<Contact> getAllContacts() {

        return service.getAllContacts();
    }

    @PutMapping("/{id}")
    public Contact updateContact(
            @PathVariable Long id,
            @RequestBody Contact contact) {

        return service.updateContact(id, contact);
    }

    @DeleteMapping("/{id}")
    public String deleteContact(
            @PathVariable Long id) {

        service.deleteContact(id);

        return "Contact deleted successfully";
    }

    @GetMapping("/search/name")
    public List<Contact> searchByName(
            @RequestParam String name) {

        return service.searchByName(name);
    }
    
    
    @GetMapping("/search/group")
    public List<Contact> searchByGroup(
            @RequestParam String group) {

        return service.searchByGroup(group);
    }
    
    @GetMapping("/search/mobile")
    public Contact searchByMobile(
            @RequestParam String mobile) {

        return service.searchByMobile(mobile);
    }

    @GetMapping("/count")
    public long countContacts() {

        return service.countContacts();
    }

    // CSV Upload API
    @PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    String filename = file.getOriginalFilename();
    
    if (filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"))) {
        return ResponseEntity.ok(csvService.uploadExcel(file));
    } else {
        return ResponseEntity.ok(csvService.uploadCSV(file));
    }
}
    // ================= GET CONTACT BY ID =================
// GET /contacts/1
@GetMapping("/{id}")
public Contact getContactById(@PathVariable Long id) {

    return service.getContactById(id);
}

// ================= AUDIT HISTORY =================
@GetMapping("/audit/{contactId}")
public List<ContactAudit> getAuditHistory(
        @PathVariable Long contactId) {

    return service.getAuditHistory(contactId);
}
}