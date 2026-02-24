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
    
    /**
     * Find contact by client ID and isPrimary flag (for email sending)
     */
    Optional<Contact> findByClientIdAndIsPrimaryAndDeletedAtIsNull(Long clientId, Boolean isPrimary);
    
    List<Contact> findByClientId(Long clientId);
}
