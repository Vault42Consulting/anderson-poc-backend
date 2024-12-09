// src/main/java/com/anderson/demo/event/ContactEvent.java
package com.anderson.demo.event;

import com.anderson.demo.model.Contact;

public class ContactEvent {
    public enum Type {
        CREATED,
        UPDATED,
        DELETED
    }

    private Type type;
    private String userId;
    private String contactId;
    private Contact contact;
    private long timestamp;

    public ContactEvent(Type type, String userId, String contactId, Contact contact) {
        this.type = type;
        this.userId = userId;
        this.contactId = contactId;
        this.contact = contact;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}