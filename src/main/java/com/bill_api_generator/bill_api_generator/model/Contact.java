package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a contact person associated with a client.
 */
@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    /**
     * Contact person's name
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Contact person's email
     */
    @Column(nullable = false)
    private String email;
    
    /**
     * Contact person's phone number
     */
    private String phone;
    
    /**
     * Job title/position at the company
     */
    private String position;
    
    /**
     * Whether this is the primary contact for the client
     */
    @Column(nullable = false)
    private Boolean isPrimary = false;
    
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
