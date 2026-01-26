package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.ClientDto;
import com.bill_api_generator.bill_api_generator.model.ClientHistory;
import com.bill_api_generator.bill_api_generator.service.ClientHistoryService;
import com.bill_api_generator.bill_api_generator.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing clients.
 * Provides CRUD operations and history tracking for client entities.
 */
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {
    
    private final ClientService clientService;
    private final ClientHistoryService clientHistoryService;
    
    /**
     * Get all active clients.
     * GET /api/clients
     */
    @GetMapping
    public ResponseEntity<List<ClientDto>> getAllClients() {
        List<ClientDto> clients = clientService.findAll();
        return ResponseEntity.ok(clients);
    }
    
    /**
     * Get a client by ID.
     * GET /api/clients/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClientDto> getClientById(@PathVariable Long id) {
        try {
            ClientDto client = clientService.findById(id);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get a client by reference code.
     * GET /api/clients/ref/{ref}
     */
    @GetMapping("/ref/{ref}")
    public ResponseEntity<ClientDto> getClientByRef(@PathVariable String ref) {
        try {
            ClientDto client = clientService.findByRef(ref);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Create a new client.
     * POST /api/clients
     */
    @PostMapping
    public ResponseEntity<ClientDto> createClient(@Valid @RequestBody ClientDto dto) {
        try {
            ClientDto createdClient = clientService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update an existing client.
     * PUT /api/clients/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClientDto> updateClient(
            @PathVariable Long id,
            @Valid @RequestBody ClientDto dto) {
        try {
            ClientDto updatedClient = clientService.update(id, dto);
            return ResponseEntity.ok(updatedClient);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete a client (soft delete).
     * DELETE /api/clients/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        try {
            clientService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Restore a deleted client.
     * POST /api/clients/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreClient(@PathVariable Long id) {
        try {
            clientService.restore(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get the history of changes for a client.
     * GET /api/clients/{id}/history
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<ClientHistory>> getClientHistory(@PathVariable Long id) {
        List<ClientHistory> history = clientHistoryService.getClientHistory(id);
        return ResponseEntity.ok(history);
    }
}
