package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.model.InvoiceRequest;
import com.bill_api_generator.bill_api_generator.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService service;

    @PostMapping(value="/docx", produces = "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
    public ResponseEntity<byte[]> docx(@RequestBody InvoiceRequest req) throws Exception {
        byte[] bytes = service.generarDocx(req);
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura-" + req.getNumeroFactura() + ".docx")
                .build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @PostMapping(value="/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(@RequestBody InvoiceRequest req) throws Exception {
        byte[] bytes = service.generarPdf(req);
        var headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("factura-" + req.getNumeroFactura() + ".pdf")
                .build());
        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}