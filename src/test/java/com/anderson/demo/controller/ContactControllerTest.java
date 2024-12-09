// src/test/java/com/anderson/demo/controller/ContactControllerTest.java
package com.anderson.demo.controller;

import com.anderson.demo.model.Contact;
import com.anderson.demo.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String AUTH_HEADER = "X-Goog-Authenticated-User-Id";

    @Test
    void shouldRejectRequestsWithoutAuthHeader() throws Exception {
        mockMvc.perform(get("/contact"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldCreateContactWithRequiredHeader() throws Exception {
        Map<String, String> contact = new HashMap<>();
        contact.put("name", "Test User");
        contact.put("email", "test@example.com");
        contact.put("customField", "customValue");

        mockMvc.perform(post("/contact")
                .header(AUTH_HEADER, "testuser123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.userId").value("testuser123"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturnEmptyContactListForNewUser() throws Exception {
        mockMvc.perform(get("/contact")
                .header(AUTH_HEADER, "newuser123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldOnlyReturnContactsForSpecificUser() throws Exception {
        // Create contacts for two different users
        createContact("user1", "Contact One");
        createContact("user2", "Contact Two");
        createContact("user1", "Contact Three");

        // Check user1's contacts
        mockMvc.perform(get("/contact")
                .header(AUTH_HEADER, "user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].name", hasItems("Contact One", "Contact Three")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Contact Two"))));

        // Check user2's contacts
        mockMvc.perform(get("/contact")
                .header(AUTH_HEADER, "user2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Contact Two"));
    }

    @Test
    void shouldAcceptArbitraryJsonFields() throws Exception {
        Map<String, Object> contact = new HashMap<>();
        contact.put("name", "Test User");
        contact.put("customString", "value");
        contact.put("customNumber", 123);
        contact.put("customBoolean", true);
        contact.put("customObject", Map.of("key", "value"));

        mockMvc.perform(post("/contact")
                .header(AUTH_HEADER, "testuser123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    private void createContact(String userId, String name) throws Exception {
        Map<String, String> contact = new HashMap<>();
        contact.put("name", name);

        mockMvc.perform(post("/contact")
                .header(AUTH_HEADER, userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact)))
                .andExpect(status().isOk());
    }
}