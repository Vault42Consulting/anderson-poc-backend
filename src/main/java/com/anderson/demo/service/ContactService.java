package com.anderson.demo.service;

import com.anderson.demo.model.Contact;
import com.anderson.demo.event.ContactEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ContactService {
    private List<Contact> contacts;
    private final String JSON_FILE = "contacts.json";
    private final ObjectMapper objectMapper;
    private final Optional<KafkaEventService> kafkaEventService;

    public ContactService(ObjectMapper objectMapper, Optional<KafkaEventService> kafkaEventService) {
        this.objectMapper = objectMapper;
        this.kafkaEventService = kafkaEventService;
        this.contacts = new ArrayList<>();
    }

    @PostConstruct
    public void init() {
        loadContacts();
    }

    private void loadContacts() {
        File file = new File(JSON_FILE);
        if (file.exists()) {
            try {
                contacts = objectMapper.readValue(file, new TypeReference<List<Contact>>() {
                });
            } catch (IOException e) {
                e.printStackTrace();
                contacts = new ArrayList<>();
            }
        }
    }

    private void saveContacts() {
        try {
            objectMapper.writeValue(new File(JSON_FILE), contacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Contact addContact(String userId, Contact contact) {
        contact.setId(UUID.randomUUID().toString());
        contact.setUserId(userId);
        contacts.add(contact);
        saveContacts();

        kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                ContactEvent.Type.CREATED,
                userId,
                contact.getId(),
                contact)));

        return contact;
    }

    public List<Contact> getContactsByUserId(String userId) {
        return contacts.stream()
                .filter(contact -> userId.equals(contact.getUserId()))
                .collect(Collectors.toList());
    }

    public Optional<Contact> updateContact(String userId, String contactId, Contact updatedContact) {
        Optional<Contact> existingContact = contacts.stream()
                .filter(c -> c.getId().equals(contactId) && c.getUserId().equals(userId))
                .findFirst();

        if (existingContact.isPresent()) {
            Contact contact = existingContact.get();
            updatedContact.setId(contactId);
            updatedContact.setUserId(userId);
            contacts.remove(contact);
            contacts.add(updatedContact);
            saveContacts();

            kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                    ContactEvent.Type.UPDATED,
                    userId,
                    contactId,
                    updatedContact)));

            return Optional.of(updatedContact);
        }
        return Optional.empty();
    }

    public boolean deleteContact(String userId, String contactId) {
        Optional<Contact> contact = contacts.stream()
                .filter(c -> c.getId().equals(contactId) && c.getUserId().equals(userId))
                .findFirst();

        if (contact.isPresent()) {
            contacts.remove(contact.get());
            saveContacts();

            kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                    ContactEvent.Type.DELETED,
                    userId,
                    contactId,
                    contact.get())));

            return true;
        }
        return false;
    }
}