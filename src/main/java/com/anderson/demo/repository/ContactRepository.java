package com.anderson.demo.repository;

import com.anderson.demo.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, String> {
    List<Contact> findByUserId(String userId);
}