package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity storing all calculations performed for an invoice.
 * Provides audit trail and optimization capabilities.
 */
@Entity
@Table(name = "invoice_calculations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceCalculation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Reference to the invoice
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, unique = true)
    private Invoice invoice;
    
    /**
     * Hourly rate used for calculation (snapshot at invoice creation)
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    /**
     * Number of hours (denormalized for audit purposes)
     */
    @Column(nullable = false)
    private Integer hours;
    
    /**
     * Subtotal: hours × rate
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    /**
     * Tax withholding rate applied (e.g., 0.15 for 15%)
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal taxWithholdingRate;
    
    /**
     * Tax withholding amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxWithholding;
    
    /**
     * VAT rate applied (e.g., 0.21 for 21%)
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal vatRate;
    
    /**
     * VAT amount
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal vat;
    
    /**
     * Total amount: subtotal - taxWithholding + vat
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;
    
    /**
     * When this calculation was performed
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime calculatedAt;
    
    /**
     * Version for handling recalculations (if rates change)
     */
    @Column(nullable = false)
    private Integer version = 1;
    
    /**
     * Optional notes about this calculation
     */
    @Column(length = 500)
    private String notes;
}
