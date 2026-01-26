package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice entity.
 * Provides database access methods for invoice operations.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByClientId(Long clientId);
    
    @Query("SELECT COALESCE(MAX(i.sequentialNumber), 69) FROM Invoice i")
    Long findMaxSequentialNumber();
    
    Optional<Invoice> findByClientIdAndDateAndHours(Long clientId, LocalDate date, Integer hours);
}
