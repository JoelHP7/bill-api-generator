package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.ClientHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ClientHistory entity.
 * Provides database access methods for client history/audit operations.
 */
@Repository
public interface ClientHistoryRepository extends JpaRepository<ClientHistory, Long> {
    
    List<ClientHistory> findByClientIdOrderByChangeDateDesc(Long clientId);
    
    List<ClientHistory> findByOperation(String operation);
}
