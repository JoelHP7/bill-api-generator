package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Client entity.
 * Provides database access methods for client operations.
 */
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    
    Optional<Client> findByTaxId(String taxId);
    
    boolean existsByTaxId(String taxId);
    
    Optional<Client> findByRef(String ref);
    
    Optional<Client> findByRefAndDeletedAtIsNull(String ref);
    
    boolean existsByRef(String ref);
    
    /**
     * Find all active clients (not soft-deleted)
     */
    List<Client> findByDeletedAtIsNull();
}
