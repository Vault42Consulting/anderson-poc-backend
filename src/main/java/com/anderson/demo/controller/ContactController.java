package com.anderson.demo.controller;

import com.anderson.demo.model.Contact;
import com.anderson.demo.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactController {
    private static final String USER_HEADER = "X-Goog-Authenticated-User-Id";
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Contact> createContact(
            @RequestHeader(USER_HEADER) String userId,
            @RequestBody Contact contact) {
        Contact savedContact = contactService.addContact(userId, contact);
        return ResponseEntity.ok(savedContact);
    }

    @GetMapping
    public ResponseEntity<List<Contact>> getContacts(
            @RequestHeader(USER_HEADER) String userId) {
        List<Contact> userContacts = contactService.getContactsByUserId(userId);
        return ResponseEntity.ok(userContacts);
    }

    @PutMapping("/{contactId}")
    public ResponseEntity<Contact> updateContact(
            @RequestHeader(USER_HEADER) String userId,
            @PathVariable String contactId,
            @RequestBody Contact contact) {
        return contactService.updateContact(userId, contactId, contact)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{contactId}")
    public ResponseEntity<Void> deleteContact(
            @RequestHeader(USER_HEADER) String userId,
            @PathVariable String contactId) {
        boolean deleted = contactService.deleteContact(userId, contactId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
