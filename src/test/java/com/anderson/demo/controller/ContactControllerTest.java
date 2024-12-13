package com.anderson.demo.controller;

import com.anderson.demo.model.Contact;
import com.anderson.demo.repository.ContactRepository;
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

        // Mock KafkaEventService so we don't need a real Kafka connection
        @MockBean
        private KafkaEventService kafkaEventService;

        @Autowired
        private ContactRepository contactRepository;

        private static final String AUTH_HEADER = "X-Backend-Service-UserId";

        @BeforeEach
        void setUp() throws Exception {
                // Clear database between tests
                contactRepository.deleteAll();
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
                contact.put("department", "Engineering");
                contact.put("location", "Seattle");
                contact.put("nickname", "Tester");

                mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(contact)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Test User"));

                // Verify in database that custom fields were saved
                mockMvc.perform(get("/contact")
                                .header(AUTH_HEADER, "testuser123"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Test User"))
                                .andExpect(jsonPath("$[0].customString").value("value"))
                                .andExpect(jsonPath("$[0].department").value("Engineering"))
                                .andExpect(jsonPath("$[0].location").value("Seattle"))
                                .andExpect(jsonPath("$[0].nickname").value("Tester"));
        }

        @Test
        void shouldRejectInvalidEmailAndPhone() throws Exception {
                // Invalid email, invalid phone
                Map<String, String> invalidContact = new HashMap<>();
                invalidContact.put("name", "Test User");
                invalidContact.put("email", "not-an-email");
                invalidContact.put("phone", "123-123");

                mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidContact)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.email").value("Invalid email format"))
                                .andExpect(jsonPath("$.phone").value(
                                                "Phone number must be in North American format (e.g., 123-456-7890)"));

                // Valid email, invalid phone
                invalidContact.put("email", "valid@email.com");
                mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidContact)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.phone").value(
                                                "Phone number must be in North American format (e.g., 123-456-7890)"))
                                .andExpect(jsonPath("$.email").doesNotExist());

                // Valid email, valid phone
                invalidContact.put("phone", "123-456-7890");
                mockMvc.perform(post("/contact")
                                .header(AUTH_HEADER, "testuser123")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidContact)))
                                .andExpect(status().isOk());
        }
}