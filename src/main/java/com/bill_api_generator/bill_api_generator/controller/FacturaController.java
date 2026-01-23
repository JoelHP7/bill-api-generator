package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.FacturaDto;
import com.bill_api_generator.bill_api_generator.service.FacturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

/**
 * Controlador REST para la generación de Facturas
 */
@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    /**
     * Genera una factura para el mes actual (devuelve JSON)
     * GET /api/facturas/generate/{clientId}?horas=160
     */
    @GetMapping("/generate/{clientId}")
    public ResponseEntity<FacturaDto> generateFacturaCurrentMonth(
            @PathVariable Long clientId,
            @RequestParam Integer horas) {
        try {
            FacturaDto factura = facturaService.generateFactura(clientId, horas, null);
            return ResponseEntity.status(HttpStatus.CREATED).body(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Genera una factura para una fecha específica (devuelve JSON)
     * GET /api/facturas/generate/{clientId}/{date}?horas=160
     * Ejemplo: /api/facturas/generate/1/2024-01-15?horas=160
     */
    @GetMapping("/generate/{clientId}/{date}")
    public ResponseEntity<FacturaDto> generateFacturaWithDate(
            @PathVariable Long clientId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer horas) {
        try {
            FacturaDto factura = facturaService.generateFactura(clientId, horas, date);
            return ResponseEntity.status(HttpStatus.CREATED).body(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Genera una factura Y descarga el documento DOCX (mes actual)
     * GET /api/facturas/generate/{clientId}/document?horas=160
     */
    @GetMapping("/generate/{clientId}/document")
    public ResponseEntity<byte[]> generateFacturaDocumentCurrentMonth(
            @PathVariable Long clientId,
            @RequestParam Integer horas) {
        try {
            ByteArrayOutputStream document = facturaService.generateFacturaWithDocument(clientId, horas, null);

            // Obtener el número de factura para el nombre del archivo
            String filename = "factura-" + LocalDate.now().getYear() + ".docx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(document.size());

            return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Genera una factura Y descarga el documento DOCX (fecha específica)
     * GET /api/facturas/generate/{clientId}/{date}/document?horas=160
     * Ejemplo: /api/facturas/generate/1/2024-01-15/document?horas=160
     */
    @GetMapping("/generate/{clientId}/{date}/document")
    public ResponseEntity<byte[]> generateFacturaDocumentWithDate(
            @PathVariable Long clientId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Integer horas) {
        try {
            ByteArrayOutputStream document = facturaService.generateFacturaWithDocument(clientId, horas, date);

            String filename = "factura-" + date.getYear() + ".docx";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(document.size());

            return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene una factura por ID (devuelve JSON)
     * GET /api/facturas/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<FacturaDto> getFacturaById(@PathVariable Long id) {
        try {
            FacturaDto factura = facturaService.findById(id);
            return ResponseEntity.ok(factura);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
