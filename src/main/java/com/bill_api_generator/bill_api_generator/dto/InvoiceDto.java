package com.bill_api_generator.bill_api_generator.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Invoice data transfer object")
public class InvoiceDto {
    
    @Schema(description = "Invoice unique identifier", example = "70")
    private Long id;
    
    @Schema(description = "Formatted invoice number", example = "2026-0070")
    private String invoiceNumber;
    
    @Schema(description = "Number of hours billed", example = "160", minimum = "1")
    private Integer hours;
    
    @Schema(description = "Invoice date", example = "2026-01-27")
    private LocalDate date;
    
    @Schema(description = "Billing period", example = "Enero 2026")
    private String billingMonth;
    
    // Client data
    @Schema(description = "Client name", example = "AKKODIS TECHNOLOGIES SPAIN S.L.U")
    private String clientName;
    
    @Schema(description = "Client tax ID", example = "B08204")
    private String clientTaxId;
    
    @Schema(description = "Client address", example = "Calle Example, 123")
    private String clientAddress;
    
    // Issuer data (fixed)
    @Schema(description = "Issuer name", example = "JOEL HERNANDEZ PLA")
    private String issuerName;
    
    @Schema(description = "Issuer address", example = "Carrer Mossen Ernest Mateu, 7")
    private String issuerAddress;
    
    @Schema(description = "Issuer postal code", example = "08181")
    private String issuerPostalCode;
    
    @Schema(description = "Issuer tax ID", example = "47818505X")
    private String issuerTaxId;
    
    @Schema(description = "Issuer IBAN", example = "ES62 1465 0180 71 1734028810")
    private String issuerIban;
    
    // Calculated amounts
    @Schema(description = "Subtotal amount (hours × rate)", example = "8000.00")
    private BigDecimal subtotal;
    
    @Schema(description = "Tax withholding (15%)", example = "1200.00")
    private BigDecimal taxWithholding;  // IRPF
    
    @Schema(description = "VAT (21%)", example = "1680.00")
    private BigDecimal vat;              // IVA
    
    @Schema(description = "Total amount to be paid", example = "8480.00")
    private BigDecimal total;
}
