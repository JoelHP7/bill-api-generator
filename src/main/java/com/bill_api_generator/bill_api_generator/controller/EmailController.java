package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.EmailRequest;
import com.bill_api_generator.bill_api_generator.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    
    private final EmailService emailService;
    
    /**
     * Envía una factura por email
     * POST /api/emails/send-factura
     * 
     * Body:
     * {
     *   "facturaId": 1,
     *   "to": "cliente@example.com",
     *   "cc": ["copia@example.com"],  // opcional
     *   "asunto": "Tu factura",        // opcional
     *   "mensaje": "Mensaje personalizado"  // opcional
     * }
     */
    @PostMapping("/send-factura")
    public ResponseEntity<Map<String, String>> sendFacturaEmail(@Valid @RequestBody EmailRequest request) {
        try {
            emailService.sendFacturaEmail(request);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "mensaje", "Email enviado exitosamente a " + request.getTo()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "status", "error",
                            "mensaje", "Error al enviar el email: " + e.getMessage()
                    ));
        }
    }
    
    /**
     * Endpoint alternativo más simple
     * POST /api/facturas/{id}/send-email?to=cliente@example.com
     */
    @PostMapping("/factura/{facturaId}/send")
    public ResponseEntity<Map<String, String>> sendFacturaEmailSimple(
            @PathVariable Long facturaId,
            @RequestParam String to,
            @RequestParam(required = false) String mensaje) {
        
        EmailRequest request = EmailRequest.builder()
                .facturaId(facturaId)
                .to(to)
                .mensaje(mensaje)
                .build();
        
        return sendFacturaEmail(request);
    }
}
