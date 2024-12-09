package com.anderson.demo.controller;

import com.anderson.demo.model.Contact;
import com.anderson.demo.service.ContactService;
import com.anderson.demo.service.KafkaEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

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

        @Autowired
        private ContactService contactService;

        // Mock KafkaEventService so we don't need a real Kafka connection
        @MockBean
        private KafkaEventService kafkaEventService;

        private static final String AUTH_HEADER = "X-Goog-Authenticated-User-Id";

        @BeforeEach
        void setUp() throws Exception {
                // Clear contacts before each test
                File file = new File("contacts.json");
                if (file.exists()) {
                        objectMapper.writeValue(file, new ArrayList<>());
                }
                // Reinitialize the service
                contactService.init();
        }

        @Test
        void shouldRejectRequestsWithoutAuthHeader() throws Exception {
                mockMvc.perform(get("/contact"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(post("/contact")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(put("/contact/123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isUnauthorized());

                mockMvc.perform(delete("/contact/123"))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldCreateAndUpdateAndDeleteContact() throws Exception {
                // Create a contact
                Map<String, String> contact = new HashMap<>();
                contact.put("name", "Test User");
                contact.put("email", "test@example.com");

                MvcResult result = mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(contact)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").exists())
                                .andExpect(jsonPath("$.userId").value("testuser123"))
                                .andExpect(jsonPath("$.name").value("Test User"))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andReturn();

                // Get the created contact's ID
                String responseJson = result.getResponse().getContentAsString();
                Contact createdContact = objectMapper.readValue(responseJson, Contact.class);
                String contactId = createdContact.getId();

                // Update the contact
                contact.put("name", "Updated User");
                mockMvc.perform(put("/contact/" + contactId)
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(contact)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(contactId))
                                .andExpect(jsonPath("$.userId").value("testuser123"))
                                .andExpect(jsonPath("$.name").value("Updated User"));

                // Delete the contact
                mockMvc.perform(delete("/contact/" + contactId)
                                .header(AUTH_HEADER, "testuser123"))
                                .andExpect(status().isOk());

                // Verify contact is deleted
                mockMvc.perform(get("/contact")
                                .header(AUTH_HEADER, "testuser123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        void shouldNotAllowUpdatingOtherUsersContacts() throws Exception {
                // Create a contact for user1
                Map<String, String> contact = new HashMap<>();
                contact.put("name", "User One Contact");

                MvcResult result = mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "user1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(contact)))
                                .andExpect(status().isOk())
                                .andReturn();

                String contactId = objectMapper.readValue(
                                result.getResponse().getContentAsString(),
                                Contact.class).getId();

                // Try to update the contact as user2
                Map<String, String> updatedContact = new HashMap<>();
                updatedContact.put("name", "Hacked Name");

                mockMvc.perform(put("/contact/" + contactId)
                                .header(AUTH_HEADER, "user2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedContact)))
                                .andExpect(status().isNotFound());

                // Try to delete the contact as user2
                mockMvc.perform(delete("/contact/" + contactId)
                                .header(AUTH_HEADER, "user2"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnNotFoundForNonexistentContact() throws Exception {
                mockMvc.perform(put("/contact/nonexistent-id")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"Test\"}"))
                                .andExpect(status().isNotFound());

                mockMvc.perform(delete("/contact/nonexistent-id")
                                .header(AUTH_HEADER, "testuser123"))
                                .andExpect(status().isNotFound());
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
}