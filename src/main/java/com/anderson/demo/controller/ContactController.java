package com.anderson.demo.controller;

import com.anderson.demo.model.Contact;
import com.anderson.demo.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
public class ContactController {
    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Contact> createContact(
            @PathVariable String userId,
            @RequestBody Contact contact) {
        Contact savedContact = contactService.addContact(userId, contact);
        return ResponseEntity.ok(savedContact);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Contact>> getContacts(@PathVariable String userId) {
        List<Contact> userContacts = contactService.getContactsByUserId(userId);
        return ResponseEntity.ok(userContacts);
    }
}