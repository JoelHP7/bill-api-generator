package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.ClienteDto;
import com.bill_api_generator.bill_api_generator.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de Clientes (CRUD)
 */
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    
    private final ClienteService clienteService;
    
    /**
     * Obtener todos los clientes
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteDto>> getAllClientes() {
        List<ClienteDto> clientes = clienteService.findAll();
        return ResponseEntity.ok(clientes);
    }
    
    /**
     * Obtener un cliente por ID
     * GET /api/clientes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDto> getClienteById(@PathVariable Long id) {
        try {
            ClienteDto cliente = clienteService.findById(id);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Crear un nuevo cliente
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ClienteDto> createCliente(@Valid @RequestBody ClienteDto clienteDto) {
        try {
            ClienteDto createdCliente = clienteService.create(clienteDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Actualizar un cliente existente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDto> updateCliente(
            @PathVariable Long id, 
            @Valid @RequestBody ClienteDto clienteDto) {
        try {
            ClienteDto updatedCliente = clienteService.update(id, clienteDto);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Eliminar un cliente
     * DELETE /api/clientes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(@PathVariable Long id) {
        try {
            clienteService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
