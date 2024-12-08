package com.anderson.demo.service;

import com.anderson.demo.model.Contact;
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

    public ContactService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
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
                contacts = objectMapper.readValue(file, new TypeReference<List<Contact>>() {});
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
        return contact;
    }

    public List<Contact> getContactsByUserId(String userId) {
        return contacts.stream()
            .filter(contact -> userId.equals(contact.getUserId()))
            .collect(Collectors.toList());
    }
}