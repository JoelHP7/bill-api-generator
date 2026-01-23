package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.ClienteDto;
import com.bill_api_generator.bill_api_generator.model.ClienteHistorico;
import com.bill_api_generator.bill_api_generator.service.ClienteHistoricoService;
import com.bill_api_generator.bill_api_generator.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {
    
    private final ClienteService clienteService;
    private final ClienteHistoricoService clienteHistoricoService;
    
    /**
     * Obtiene todos los clientes activos
     * GET /api/clientes
     */
    @GetMapping
    public ResponseEntity<List<ClienteDto>> getAllClientes() {
        List<ClienteDto> clientes = clienteService.findAll();
        return ResponseEntity.ok(clientes);
    }
    
    /**
     * Obtiene un cliente por ID
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
     * Obtiene un cliente por REF
     * GET /api/clientes/ref/{ref}
     */
    @GetMapping("/ref/{ref}")
    public ResponseEntity<ClienteDto> getClienteByRef(@PathVariable String ref) {
        try {
            ClienteDto cliente = clienteService.findByRef(ref);
            return ResponseEntity.ok(cliente);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Crea un nuevo cliente
     * POST /api/clientes
     */
    @PostMapping
    public ResponseEntity<ClienteDto> createCliente(@Valid @RequestBody ClienteDto dto) {
        try {
            ClienteDto createdCliente = clienteService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Actualiza un cliente
     * PUT /api/clientes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDto> updateCliente(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDto dto) {
        try {
            ClienteDto updatedCliente = clienteService.update(id, dto);
            return ResponseEntity.ok(updatedCliente);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Elimina un cliente (soft delete)
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
    
    /**
     * Restaura un cliente eliminado
     * POST /api/clientes/{id}/restore
     */
    @PostMapping("/{id}/restore")
    public ResponseEntity<Void> restoreCliente(@PathVariable Long id) {
        try {
            clienteService.restore(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obtiene el histórico de cambios de un cliente
     * GET /api/clientes/{id}/historico
     */
    @GetMapping("/{id}/historico")
    public ResponseEntity<List<ClienteHistorico>> getHistoricoCliente(@PathVariable Long id) {
        List<ClienteHistorico> historico = clienteHistoricoService.getHistorialCliente(id);
        return ResponseEntity.ok(historico);
    }
}
