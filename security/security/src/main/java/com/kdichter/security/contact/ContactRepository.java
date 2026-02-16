package com.kdichter.security.contact;

import com.kdichter.security.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, String> {
    Optional<Contact> findById(String id);

    // Find all contacts for a specific user (with pagination)
    Page<Contact> findByUser(User user, Pageable pageable);

    // Find a specific contact by ID and user (ensures user owns the contact)
    Optional<Contact> findByIdAndUser(String id, User user);
}
