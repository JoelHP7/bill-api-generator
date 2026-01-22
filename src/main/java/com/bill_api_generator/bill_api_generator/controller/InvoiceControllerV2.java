package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.service.InvoiceGeneratorService;
import com.bill_api_generator.bill_api_generator.service.PdfInvoiceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v2/invoice")
public class InvoiceControllerV2 {

    @Autowired
    private InvoiceGeneratorService invoiceGeneratorService;

    @Autowired
    private PdfInvoiceGeneratorService pdfInvoiceGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateInvoice(
            @RequestBody InvoiceGeneratorService.InvoiceData invoiceData,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            byte[] documentBytes;
            String fileExtension;
            MediaType mediaType;

            if ("docx".equalsIgnoreCase(format)) {
                documentBytes = invoiceGeneratorService.generateInvoiceDocx(invoiceData);
                fileExtension = "docx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                documentBytes = pdfInvoiceGeneratorService.generateInvoicePdf(invoiceData);
                fileExtension = "pdf";
                mediaType = MediaType.APPLICATION_PDF;
            }

            String filename = String.format("factura_%s_%s.%s",
                    invoiceData.getInvoiceNumber(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    fileExtension);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(documentBytes.length);

            return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/generate-sample")
    public ResponseEntity<byte[]> generateSampleInvoice(
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            InvoiceGeneratorService.InvoiceData sampleData = new InvoiceGeneratorService.InvoiceData(); // Usa los valores por defecto
            byte[] documentBytes;
            String fileExtension;
            MediaType mediaType;

            if ("docx".equalsIgnoreCase(format)) {
                documentBytes = invoiceGeneratorService.generateInvoiceDocx(sampleData);
                fileExtension = "docx";
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            } else {
                documentBytes = pdfInvoiceGeneratorService.generateInvoicePdf(sampleData);
                fileExtension = "pdf";
                mediaType = MediaType.APPLICATION_PDF;
            }

            String filename = String.format("factura_sample_%s.%s",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    fileExtension);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(documentBytes.length);

            return new ResponseEntity<>(documentBytes, headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/save-to-file")
    public ResponseEntity<String> saveInvoiceToFile(
            @RequestBody InvoiceGeneratorService.InvoiceData invoiceData,
            @RequestParam(defaultValue = "/tmp/") String outputPath,
            @RequestParam(defaultValue = "pdf") String format) {
        try {
            String fileExtension = "docx".equalsIgnoreCase(format) ? "docx" : "pdf";
            String filename = String.format("factura_%s_%s.%s",
                    invoiceData.getInvoiceNumber(),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                    fileExtension);

            String fullPath = outputPath + filename;

            if ("docx".equalsIgnoreCase(format)) {
                invoiceGeneratorService.saveInvoiceToFile(invoiceData, fullPath);
            } else {
                pdfInvoiceGeneratorService.savePdfInvoiceToFile(invoiceData, fullPath);
            }

            return ResponseEntity.ok("Factura guardada en: " + fullPath);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la factura: " + e.getMessage());
        }
    }
}