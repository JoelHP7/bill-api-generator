package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.EmailRequest;
import com.bill_api_generator.bill_api_generator.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * REST Controller for managing email operations.
 * Provides endpoints for sending invoices via email.
 */
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Emails", description = "Email sending endpoints")
public class EmailController {
    
    private final EmailService emailService;
    
    @Operation(
        summary = "Send an invoice via email",
        description = "Sends a generated invoice to a specified email address with optional CC recipients and custom message"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email sent successfully"),
        @ApiResponse(responseCode = "500", description = "Error sending email")
    })
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
    
    @Operation(
        summary = "Send invoice email (simplified)",
        description = "Alternative simpler endpoint for sending invoice emails"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email sent successfully"),
        @ApiResponse(responseCode = "500", description = "Error sending email")
    })
    @PostMapping("/invoice/{invoiceId}/send")
    public ResponseEntity<Map<String, String>> sendInvoiceEmailSimple(
            @Parameter(description = "Invoice ID", required = true)
            @PathVariable Long invoiceId,
            
            @Parameter(description = "Recipient email", required = true)
            @RequestParam String to,
            
            @Parameter(description = "Custom message")
            @RequestParam(required = false) String message) {
        
        EmailRequest request = EmailRequest.builder()
                .invoiceId(invoiceId)
                .to(to)
                .message(message)
                .build();
        
        return sendInvoiceEmail(request);
    }
    
    @Operation(
        summary = "Send PDF invoice by client reference",
        description = "Sends a PDF invoice to the primary contact of a client. " +
                     "Automatically retrieves email configuration from the client's primary contact."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Email sent successfully"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid PDF file or missing required data"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Client not found or no primary contact configured"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error sending email"
        )
    })
    @PostMapping(value = "/send-pdf/{clientRef}", consumes = "multipart/form-data")
    public ResponseEntity<Map<String, Object>> sendPdfInvoice(
            @Parameter(description = "Client reference code", required = true, example = "ACC001")
            @PathVariable String clientRef,
            
            @Parameter(description = "PDF invoice file", required = true)
            @RequestParam("file") MultipartFile pdfFile
    ) {
        log.info("Received request to send PDF invoice for client: {}", clientRef);
        
        try {
            // Validate PDF file
            if (pdfFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "status", "ERROR",
                            "message", "PDF file is required"
                        ));
            }
            
            String contentType = pdfFile.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                            "status", "ERROR",
                            "message", "File must be a PDF. Received: " + contentType
                        ));
            }
            
            // Send email using the service
            emailService.sendPdfByClientRef(clientRef, pdfFile);
            
            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Email sent successfully",
                "clientRef", clientRef,
                "filename", pdfFile.getOriginalFilename() != null ? pdfFile.getOriginalFilename() : "unknown"
            ));
            
        } catch (RuntimeException e) {
            log.error("Error sending PDF for client {}: {}", clientRef, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "status", "ERROR",
                        "message", e.getMessage()
                    ));
        } catch (MessagingException | IOException e) {
            log.error("Error sending email for client {}", clientRef, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                        "status", "ERROR",
                        "message", "Error sending email: " + e.getMessage()
                    ));
        }
    }
}
