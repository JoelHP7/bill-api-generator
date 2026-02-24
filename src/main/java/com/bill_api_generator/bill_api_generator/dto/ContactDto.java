package com.bill_api_generator.bill_api_generator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Contact entity.
 * Used for API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Contact data transfer object")
public class ContactDto {
    
    @Schema(description = "Contact unique identifier", example = "1")
    private Long id;
    
    @Schema(description = "Client ID", example = "1")
    private Long clientId;
    
    @NotBlank(message = "Name is required")
    @Schema(description = "Contact person's name", example = "John Doe", required = true)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Schema(description = "Contact email address", example = "john.doe@company.com", required = true)
    private String email;
    
    @Schema(description = "Contact phone number", example = "+34 600 123 456")
    private String phone;
    
    @Schema(description = "Job position", example = "Accounting Manager")
    private String position;
    
    @Schema(description = "Whether this is the primary contact", example = "true")
    private Boolean isPrimary;
    
    // New email configuration fields
    
    @Schema(description = "CC email addresses for invoices", 
            example = "[\"accounting@company.com\", \"manager@company.com\"]")
    private List<@Email String> emailCc;
    
    @Schema(description = "Default email subject template. Supports placeholders: {invoiceNumber}, {clientName}, {date}", 
            example = "Invoice {invoiceNumber} - {clientName}")
    private String emailSubject;
    
    @Schema(description = "Default email message body. Supports placeholders: {clientName}, {invoiceNumber}, {total}", 
            example = "Dear {clientName}, please find attached invoice...")
    private String emailMessage;
    
    @Schema(description = "Auto-send invoices to this contact", example = "false")
    private Boolean autoSendEmails;
    
    // Audit fields (read-only)
    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
    
    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Deletion timestamp (soft delete)")
    private LocalDateTime deletedAt;
}
