package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Contact entity.
 * Provides database access methods for contact operations.
 */
@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    List<Contact> findByClientIdAndDeletedAtIsNull(Long clientId);
    
    Optional<Contact> findByClientIdAndIsPrimaryTrueAndDeletedAtIsNull(Long clientId);
    
    List<Contact> findByClientId(Long clientId);
}
