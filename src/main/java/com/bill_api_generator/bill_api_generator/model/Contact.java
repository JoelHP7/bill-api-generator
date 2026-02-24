package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Entity representing a contact person associated with a client.
 * Extended to include email configuration for invoice sending.
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
    
    // ========== NEW FIELDS FOR EMAIL CONFIGURATION ==========
    
    /**
     * Carbon copy recipients (comma-separated emails)
     * Example: "accounting@company.com,manager@company.com"
     */
    @Column(name = "email_cc", length = 500)
    private String emailCc;
    
    /**
     * Default email subject template for invoices
     * Can use placeholders: {invoiceNumber}, {clientName}, {date}
     * Example: "Invoice {invoiceNumber} - {clientName}"
     */
    @Column(name = "email_subject", length = 200)
    private String emailSubject;
    
    /**
     * Default email message body
     * Can use placeholders: {clientName}, {invoiceNumber}, {total}
     */
    @Column(name = "email_message", length = 2000)
    private String emailMessage;
    
    /**
     * Whether to send emails to this contact automatically
     */
    @Column(name = "auto_send_emails", nullable = false)
    private Boolean autoSendEmails = false;
    
    // ========== END OF NEW FIELDS ==========
    
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
    
    /**
     * Helper method to get CC emails as a list
     */
    public List<String> getEmailCcList() {
        if (emailCc == null || emailCc.isBlank()) {
            return List.of();
        }
        return Arrays.stream(emailCc.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
    
    /**
     * Helper method to set CC emails from a list
     */
    public void setEmailCcList(List<String> ccEmails) {
        if (ccEmails == null || ccEmails.isEmpty()) {
            this.emailCc = null;
        } else {
            this.emailCc = String.join(",", ccEmails);
        }
    }
}
