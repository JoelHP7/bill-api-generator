package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing an invoice issued to a client.
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    /**
     * Global sequential number (70, 71, 72...)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_number_seq")
    @SequenceGenerator(
        name = "invoice_number_seq",
        sequenceName = "invoice_sequential_number",
        initialValue = 70,
        allocationSize = 1
    )
    @Column(name = "sequential_number")
    private Long sequentialNumber;
    
    /**
     * Formatted invoice number (e.g., "2026-0070", "2026-0071"...)
     */
    @Column(nullable = false, unique = true)
    private String invoiceNumber;
    
    /**
     * Number of hours billed
     */
    @Column(nullable = false)
    private Integer hours;
    
    /**
     * Client associated with this invoice
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    /**
     * Invoice date
     */
    @Column(nullable = false)
    private LocalDate date;
}
