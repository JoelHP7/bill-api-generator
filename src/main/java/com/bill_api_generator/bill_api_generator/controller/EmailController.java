package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.EmailRequest;
import com.bill_api_generator.bill_api_generator.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for managing email operations.
 * Provides endpoints for sending invoices via email.
 */
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    
    private final EmailService emailService;
    
    /**
     * Send an invoice via email.
     * POST /api/emails/send-invoice
     * 
     * Body:
     * {
     *   "invoiceId": 1,
     *   "to": "client@example.com",
     *   "cc": ["copy@example.com"],  // optional
     *   "subject": "Your Invoice",   // optional
     *   "message": "Custom message"  // optional
     * }
     */
    @PostMapping("/send-invoice")
    public ResponseEntity<Map<String, String>> sendInvoiceEmail(@Valid @RequestBody EmailRequest request) {
        try {
            emailService.sendInvoiceEmail(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Email sent successfully to " + request.getTo()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "message", "Error sending email: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Alternative simpler endpoint.
     * POST /api/invoices/{id}/send-email?to=client@example.com
     */
    @PostMapping("/invoice/{invoiceId}/send")
    public ResponseEntity<Map<String, String>> sendInvoiceEmailSimple(
            @PathVariable Long invoiceId,
            @RequestParam String to,
            @RequestParam(required = false) String message) {
        
        EmailRequest request = EmailRequest.builder()
                .invoiceId(invoiceId)
                .to(to)
                .message(message)
                .build();
        
        return sendInvoiceEmail(request);
    }
}
