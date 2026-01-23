package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.ContactoDto;
import com.bill_api_generator.bill_api_generator.service.ContactoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contactos")
@RequiredArgsConstructor
public class ContactoController {
    
    private final ContactoService contactoService;
    
    /**
     * Obtiene todos los contactos de un cliente
     * GET /api/contactos/cliente/{clienteRef}
     */
    @GetMapping("/cliente/{clienteRef}")
    public ResponseEntity<List<ContactoDto>> getContactosByCliente(@PathVariable String clienteRef) {
        try {
            List<ContactoDto> contactos = contactoService.findByClienteRef(clienteRef);
            return ResponseEntity.ok(contactos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene el contacto principal de un cliente
     * GET /api/contactos/cliente/{clienteRef}/principal
     */
    @GetMapping("/cliente/{clienteRef}/principal")
    public ResponseEntity<ContactoDto> getContactoPrincipal(@PathVariable String clienteRef) {
        try {
            return contactoService.findContactoPrincipal(clienteRef)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene un contacto por ID
     * GET /api/contactos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactoDto> getContactoById(@PathVariable Long id) {
        try {
            ContactoDto contacto = contactoService.findById(id);
            return ResponseEntity.ok(contacto);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Crea un contacto para un cliente
     * POST /api/contactos/cliente/{clienteRef}
     */
    @PostMapping("/cliente/{clienteRef}")
    public ResponseEntity<ContactoDto> createContacto(
            @PathVariable String clienteRef,
            @Valid @RequestBody ContactoDto dto) {
        try {
            ContactoDto createdContacto = contactoService.create(clienteRef, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdContacto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Actualiza un contacto
     * PUT /api/contactos/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContactoDto> updateContacto(
            @PathVariable Long id,
            @Valid @RequestBody ContactoDto dto) {
        try {
            ContactoDto updatedContacto = contactoService.update(id, dto);
            return ResponseEntity.ok(updatedContacto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Elimina un contacto (soft delete)
     * DELETE /api/contactos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContacto(@PathVariable Long id) {
        try {
            contactoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
