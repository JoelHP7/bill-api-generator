package com.bill_api_generator.bill_api_generator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Data Transfer Object for email sending requests.
 * Used to send invoices via email to clients.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    
    @NotNull(message = "Invoice ID is required")
    private Long invoiceId;
    
    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email")
    private String to;
    
    /**
     * Carbon copy recipients
     */
    private List<@Email String> cc;
    
    /**
     * Email subject (if null, will be generated automatically)
     */
    private String subject;
    
    /**
     * Additional custom message body
     */
    private String message;
}
