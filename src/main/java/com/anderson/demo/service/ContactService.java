package com.anderson.demo.service;

import com.anderson.demo.model.Contact;
import com.anderson.demo.event.ContactEvent;
import com.anderson.demo.repository.ContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Transactional
public class ContactService {
    private final ContactRepository contactRepository;
    private final Optional<KafkaEventService> kafkaEventService;

    public ContactService(ContactRepository contactRepository,
            Optional<KafkaEventService> kafkaEventService) {
        this.contactRepository = contactRepository;
        this.kafkaEventService = kafkaEventService;
    }

    public Contact addContact(String userId, Contact contact) {
        contact.setId(UUID.randomUUID().toString());
        contact.setUserId(userId);
        Contact savedContact = contactRepository.save(contact);

        kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                ContactEvent.Type.CREATED,
                userId,
                contact.getId(),
                contact)));

        return savedContact;
    }

    public List<Contact> getContactsByUserId(String userId) {
        return contactRepository.findByUserId(userId);
    }

    public Optional<Contact> updateContact(String userId, String contactId, Contact updatedContact) {
        return contactRepository.findById(contactId)
                .filter(contact -> contact.getUserId().equals(userId))
                .map(contact -> {
                    updatedContact.setId(contactId);
                    updatedContact.setUserId(userId);
                    Contact saved = contactRepository.save(updatedContact);

                    kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                            ContactEvent.Type.UPDATED,
                            userId,
                            contactId,
                            saved)));

                    return saved;
                });
    }

    public boolean deleteContact(String userId, String contactId) {
        return contactRepository.findById(contactId)
                .filter(contact -> contact.getUserId().equals(userId))
                .map(contact -> {
                    contactRepository.delete(contact);

                    kafkaEventService.ifPresent(service -> service.sendEvent(new ContactEvent(
                            ContactEvent.Type.DELETED,
                            userId,
                            contactId,
                            contact)));

                    return true;
                })
                .orElse(false);
    }
}