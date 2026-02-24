package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.InvoiceDto;
import com.bill_api_generator.bill_api_generator.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing invoices.
 * Provides endpoints for invoice generation, retrieval, and document download.
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * ========================================
     * NEW ENDPOINTS WITH REF (RECOMMENDED)
     * ========================================
     */

    /**
     * Generate an invoice using client reference.
     * GET /api/invoices/generate/ref/{clientRef}?hours=160
     *
     * Returns:
     * - status: CREATED | DUPLICATE
     * - message: Description
     * - invoice: InvoiceDto
     * - suggestions: [] (only if duplicate)
     */
    @GetMapping("/generate/ref/{clientRef}")
    public ResponseEntity<Map<String, Object>> generateInvoiceByRef(
            @PathVariable String clientRef,
            @RequestParam Integer hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = invoiceService.generateInvoiceByRef(clientRef, hours, date);

        String status = (String) response.get("status");
        HttpStatus httpStatus = "CREATED".equals(status) ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Generate an invoice AND download the DOCX document using client reference.
     * GET /api/invoices/generate/ref/{clientRef}/document?hours=160
     */
    @GetMapping("/generate/ref/{clientRef}/document")
    public ResponseEntity<byte[]> generateInvoiceDocumentByRef(
            @PathVariable String clientRef,
            @RequestParam Integer hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ByteArrayOutputStream document = invoiceService.generateInvoiceWithDocumentByRef(clientRef, hours, date);

        String filename = "invoice-" + clientRef + "-" +
                (date != null ? date.getYear() : LocalDate.now().getYear()) + ".docx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(document.size());

        return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * Get all invoices for a client by reference.
     * GET /api/invoices/client/{clientRef}
     */
    @GetMapping("/client/{clientRef}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByClientRef(@PathVariable String clientRef) {
        List<InvoiceDto> invoices = invoiceService.findByClientRef(clientRef);
        return ResponseEntity.ok(invoices);
    }

    /**
     * ========================================
     * LEGACY ENDPOINTS WITH ID (COMPATIBILITY)
     * ========================================
     */

    /**
     * Generate an invoice using client ID (legacy).
     * GET /api/invoices/generate/{clientId}?hours=160
     */
    @GetMapping("/generate/{clientId}")
    public ResponseEntity<Map<String, Object>> generateInvoice(
            @PathVariable Long clientId,
            @RequestParam Integer hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = invoiceService.generateInvoice(clientId, hours, date);

        String status = (String) response.get("status");
        HttpStatus httpStatus = "CREATED".equals(status) ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Generate document using client ID (legacy).
     * GET /api/invoices/generate/{clientId}/document?hours=160
     */
    @GetMapping("/generate/{clientId}/document")
    public ResponseEntity<byte[]> generateInvoiceDocument(
            @PathVariable Long clientId,
            @RequestParam Integer hours,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ByteArrayOutputStream document = invoiceService.generateInvoiceWithDocument(clientId, hours, date);

        String filename = "invoice-" + (date != null ? date.getYear() : LocalDate.now().getYear()) + ".docx";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(document.size());

        return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * ========================================
     * COMMON ENDPOINTS
     * ========================================
     */

    /**
     * Get an invoice by ID.
     * GET /api/invoices/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.findById(id);
        return ResponseEntity.ok(invoice);
    }
}
