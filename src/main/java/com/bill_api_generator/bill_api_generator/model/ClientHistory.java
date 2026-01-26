package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the historical changes made to clients.
 * Used for audit trail and change tracking.
 */
@Entity
@Table(name = "client_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * ID of the modified client
     */
    @Column(nullable = false)
    private Long clientId;
    
    @Column(nullable = false, length = 50)
    private String ref;
    
    private String name;
    
    private String taxId;
    
    private String address;
    
    private String postalCode;
    
    private BigDecimal rate;
    
    /**
     * Type of operation: INSERT, UPDATE, DELETE
     */
    @Column(nullable = false, length = 20)
    private String operation;
    
    /**
     * Timestamp of the change
     */
    @Column(nullable = false)
    private LocalDateTime changeDate;
    
    /**
     * User who made the modification (for future authentication cases)
     */
    private String modifiedBy;
    
    /**
     * JSON with the modified fields details
     */
    @Column(columnDefinition = "TEXT")
    private String changeDetails;
}
