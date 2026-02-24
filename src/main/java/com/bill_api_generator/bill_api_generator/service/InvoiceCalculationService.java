package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.model.Invoice;
import com.bill_api_generator.bill_api_generator.model.InvoiceCalculation;
import com.bill_api_generator.bill_api_generator.repository.InvoiceCalculationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Service for managing invoice calculations.
 * Handles calculation, storage, and retrieval of invoice financial data.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceCalculationService {
    
    private final InvoiceCalculationRepository calculationRepository;
    
    // Tax rate constants
    private static final BigDecimal TAX_WITHHOLDING_RATE = new BigDecimal("0.15"); // 15% IRPF
    private static final BigDecimal VAT_RATE = new BigDecimal("0.21"); // 21% IVA
    
    /**
     * Calculate and store invoice calculations.
     * If calculation already exists, returns the existing one (optimization).
     *
     * @param invoice The invoice to calculate
     * @param client The client (for rate information)
     * @return The saved calculation
     */
    @Transactional
    public InvoiceCalculation calculateAndStore(Invoice invoice, Client client) {
        // Check if calculation already exists (optimization)
        Optional<InvoiceCalculation> existing = calculationRepository
                .findByInvoiceSequentialNumber(invoice.getSequentialNumber());
        
        if (existing.isPresent()) {
            log.debug("Using existing calculation for invoice: {}", invoice.getInvoiceNumber());
            return existing.get();
        }
        
        // Perform calculations
        BigDecimal hourlyRate = client.getRate();
        BigDecimal subtotal = hourlyRate.multiply(new BigDecimal(invoice.getHours()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxWithholding = subtotal.multiply(TAX_WITHHOLDING_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal vat = subtotal.multiply(VAT_RATE)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.subtract(taxWithholding).add(vat)
                .setScale(2, RoundingMode.HALF_UP);
        
        // Create and save calculation
        InvoiceCalculation calculation = InvoiceCalculation.builder()
                .invoice(invoice)
                .hourlyRate(hourlyRate)
                .hours(invoice.getHours())
                .subtotal(subtotal)
                .taxWithholdingRate(TAX_WITHHOLDING_RATE)
                .taxWithholding(taxWithholding)
                .vatRate(VAT_RATE)
                .vat(vat)
                .total(total)
                .version(1)
                .build();
        
        InvoiceCalculation saved = calculationRepository.save(calculation);
        log.info("Calculation stored for invoice: {}", invoice.getInvoiceNumber());
        
        return saved;
    }
    
    /**
     * Get calculation for an invoice.
     *
     * @param invoiceId Invoice sequential number
     * @return The calculation
     * @throws RuntimeException if calculation not found
     */
    @Transactional(readOnly = true)
    public InvoiceCalculation getCalculation(Long invoiceId) {
        return calculationRepository.findByInvoiceSequentialNumber(invoiceId)
                .orElseThrow(() -> new RuntimeException(
                    "Calculation not found for invoice: " + invoiceId));
    }
    
    /**
     * Recalculate an invoice (useful if rates change).
     *
     * @param invoice The invoice to recalculate
     * @param client The client
     * @param notes Optional notes about why recalculation was needed
     * @return The new calculation
     */
    @Transactional
    public InvoiceCalculation recalculate(Invoice invoice, Client client, String notes) {
        // Get existing calculation to increment version
        Optional<InvoiceCalculation> existing = calculationRepository
                .findByInvoiceSequentialNumber(invoice.getSequentialNumber());
        
        int newVersion = existing.map(calc -> calc.getVersion() + 1).orElse(1);
        
        // Delete old calculation if exists
        existing.ifPresent(calculationRepository::delete);
        
        // Perform new calculation
        InvoiceCalculation newCalculation = calculateAndStore(invoice, client);
        newCalculation.setVersion(newVersion);
        newCalculation.setNotes(notes);
        
        return calculationRepository.save(newCalculation);
    }
}
