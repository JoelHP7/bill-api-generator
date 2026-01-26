package com.bill_api_generator.bill_api_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for Invoice entity.
 * Contains invoice details along with calculated amounts and client information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    
    private Long id;
    private String invoiceNumber;
    private Integer hours;
    private LocalDate date;
    private String billingMonth;
    
    // Client data
    private String clientName;
    private String clientTaxId;
    private String clientAddress;
    
    // Issuer data (fixed)
    private String issuerName;
    private String issuerAddress;
    private String issuerPostalCode;
    private String issuerTaxId;
    private String issuerIban;
    
    // Calculated amounts
    private BigDecimal subtotal;
    private BigDecimal taxWithholding;  // IRPF
    private BigDecimal vat;              // IVA
    private BigDecimal total;
}
