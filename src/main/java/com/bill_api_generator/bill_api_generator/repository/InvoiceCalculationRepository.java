package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.InvoiceCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for InvoiceCalculation entity operations.
 */
@Repository
public interface InvoiceCalculationRepository extends JpaRepository<InvoiceCalculation, Long> {
    
    /**
     * Find calculation by invoice sequential number
     */
    Optional<InvoiceCalculation> findByInvoiceSequentialNumber(Long invoiceId);
    
    /**
     * Check if calculation exists for invoice
     */
    boolean existsByInvoiceSequentialNumber(Long invoiceId);
}
