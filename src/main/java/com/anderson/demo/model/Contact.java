package com.anderson.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?1?[-.]?\\(?([0-9]{3})\\)?[-.]?([0-9]{3})[-.]?([0-9]{4})$", message = "Phone number must be in North American format (e.g., 123-456-7890)")
    private String phone;

    @ElementCollection
    @CollectionTable(name = "contact_attributes", joinColumns = @JoinColumn(name = "contact_id"))
    @MapKeyColumn(name = "attr_key")
    @Column(name = "attr_value")
    private Map<String, String> additionalAttributes = new HashMap<>();

    // Existing getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // For handling arbitrary fields
    @JsonAnySetter
    public void set(String key, String value) {
        if (!isBasicAttribute(key)) {
            additionalAttributes.put(key, value);
        }
    }

    @JsonAnyGetter
    public Map<String, String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    private boolean isBasicAttribute(String key) {
        return key.equals("id") || key.equals("userId") ||
                key.equals("name") || key.equals("email") ||
                key.equals("phone");
    }
}