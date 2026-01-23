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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/facturas")
@RequiredArgsConstructor
public class FacturaController {
    
    private final FacturaService facturaService;
    
    /**
     * ========================================
     * ENDPOINTS NUEVOS CON REF (RECOMENDADO)
     * ========================================
     */
    
    /**
     * Genera una factura usando REF del cliente
     * GET /api/facturas/generate/ref/{clienteRef}?horas=160
     * 
     * Retorna:
     * - status: CREADA | DUPLICADA
     * - mensaje: Descripción
     * - factura: FacturaDto
     * - sugerencias: [] (solo si es duplicada)
     */
    @GetMapping("/generate/ref/{clienteRef}")
    public ResponseEntity<Map<String, Object>> generateFacturaByRef(
            @PathVariable String clienteRef,
            @RequestParam Integer horas,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            Map<String, Object> response = facturaService.generateFacturaByRef(clienteRef, horas, fecha);
            
            String status = (String) response.get("status");
            HttpStatus httpStatus = "CREADA".equals(status) ? HttpStatus.CREATED : HttpStatus.OK;
            
            return ResponseEntity.status(httpStatus).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Genera una factura Y descarga el documento DOCX usando REF
     * GET /api/facturas/generate/ref/{clienteRef}/document?horas=160
     */
    @GetMapping("/generate/ref/{clienteRef}/document")
    public ResponseEntity<byte[]> generateFacturaDocumentByRef(
            @PathVariable String clienteRef,
            @RequestParam Integer horas,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            ByteArrayOutputStream document = facturaService.generateFacturaWithDocumentByRef(clienteRef, horas, fecha);
            
            String filename = "factura-" + clienteRef + "-" + 
                    (fecha != null ? fecha.getYear() : LocalDate.now().getYear()) + ".docx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(document.size());
            
            return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene todas las facturas de un cliente por REF
     * GET /api/facturas/cliente/{clienteRef}
     */
    @GetMapping("/cliente/{clienteRef}")
    public ResponseEntity<List<FacturaDto>> getFacturasByClienteRef(@PathVariable String clienteRef) {
        try {
            List<FacturaDto> facturas = facturaService.findByClienteRef(clienteRef);
            return ResponseEntity.ok(facturas);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * ========================================
     * ENDPOINTS LEGACY CON ID (COMPATIBILIDAD)
     * ========================================
     */
    
    /**
     * Genera una factura usando ID del cliente (legacy)
     * GET /api/facturas/generate/{clientId}?horas=160
     */
    @GetMapping("/generate/{clientId}")
    public ResponseEntity<Map<String, Object>> generateFactura(
            @PathVariable Long clientId,
            @RequestParam Integer horas,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            Map<String, Object> response = facturaService.generateFactura(clientId, horas, fecha);
            
            String status = (String) response.get("status");
            HttpStatus httpStatus = "CREADA".equals(status) ? HttpStatus.CREATED : HttpStatus.OK;
            
            return ResponseEntity.status(httpStatus).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Genera documento usando ID (legacy)
     * GET /api/facturas/generate/{clientId}/document?horas=160
     */
    @GetMapping("/generate/{clientId}/document")
    public ResponseEntity<byte[]> generateFacturaDocument(
            @PathVariable Long clientId,
            @RequestParam Integer horas,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            ByteArrayOutputStream document = facturaService.generateFacturaWithDocument(clientId, horas, fecha);
            
            String filename = "factura-" + (fecha != null ? fecha.getYear() : LocalDate.now().getYear()) + ".docx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(document.size());
            
            return new ResponseEntity<>(document.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * ========================================
     * ENDPOINTS COMUNES
     * ========================================
     */
    
    /**
     * Obtiene una factura por ID
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
