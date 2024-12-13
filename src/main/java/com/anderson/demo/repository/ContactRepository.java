package com.anderson.demo.repository;

import com.anderson.demo.model.Contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, String> {
    @Query(value = "SELECT * FROM contacts WHERE user_id = ?1 " +
            "ORDER BY COALESCE(SPLIT_PART(name, ' ', -1), name)", nativeQuery = true)
    List<Contact> findByUserIdOrderedByLastName(String userId);

    @Query(value = "SELECT * FROM contacts WHERE user_id = ?1 ORDER BY name", nativeQuery = true)
    List<Contact> findByUserIdOrderedByFullName(String userId);
}