package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.model.InvoiceRequest;
import com.bill_api_generator.bill_api_generator.service.InvoiceService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    // Genera DOCX y devuelve la ruta
    @PostMapping(value = "/docx", produces = "application/json")
    public ResponseEntity<FileLocationResponse> generateDocx(@RequestBody InvoiceRequest req) throws Exception {
        Path out = service.generarDocx(req); // escribe en C:/workspace/...
        return ResponseEntity.ok(new FileLocationResponse(out.toString()));
    }

    // Genera PDF y devuelve la ruta
    @PostMapping(value = "/pdf", produces = "application/json")
    public ResponseEntity<FileLocationResponse> generatePdf(@RequestBody InvoiceRequest req) throws Exception {
        Path out = service.generarPdf(req); // escribe en C:/workspace/...
        return ResponseEntity.ok(new FileLocationResponse(out.toString()));
    }

    // Genera ambos (DOCX + PDF) y devuelve ambas rutas
    @PostMapping(value = "/both", produces = "application/json")
    public ResponseEntity<BothFilesResponse> generateBoth(@RequestBody InvoiceRequest req) throws Exception {
        Path docx = service.generarDocx(req);
        Path pdf  = service.generarPdf(req);
        return ResponseEntity.ok(new BothFilesResponse(docx.toString(), pdf.toString()));
    }

    // Manejo simple de errores -> JSON legible
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("ERROR_GENERATING_INVOICE", ex.getMessage()));
    }

    // ---- DTOs de respuesta ----
    @Data @AllArgsConstructor
    static class FileLocationResponse {
        private String path; // p.ej. C:/workspace/factura-52.docx
    }

    @Data @AllArgsConstructor
    static class BothFilesResponse {
        private String docxPath; // p.ej. C:/workspace/factura-52.docx
        private String pdfPath;  // p.ej. C:/workspace/factura-52.pdf
    }

    @Data @AllArgsConstructor
    static class ErrorResponse {
        private String code;
        private String message;
    }
}