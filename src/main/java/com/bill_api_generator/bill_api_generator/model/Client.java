package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a client/customer in the system.
 */
@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Unique reference code for the client (e.g., "AKKODIS", "TECH_SOL")
     */
    @Column(nullable = false, unique = true, length = 50)
    private String ref;
    
    /**
     * Client's name (e.g., "AXPE CONSULTING, S.L.")
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Tax identification number (e.g., "B84184548")
     */
    @Column(nullable = false, unique = true)
    private String taxId;
    
    /**
     * Client's address (e.g., "Calle Arturo Soria 122, Madrid")
     */
    @Column(nullable = false)
    private String address;
    
    /**
     * Postal code (e.g., "08204")
     */
    private String postalCode;
    
    /**
     * Hourly rate for this client
     */
    @Column(nullable = false)
    private BigDecimal rate;
    
    // Audit fields
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Soft delete timestamp
     */
    @Column
    private LocalDateTime deletedAt;
}
