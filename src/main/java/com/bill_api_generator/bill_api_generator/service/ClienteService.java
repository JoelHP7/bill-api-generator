package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.ClienteDto;
import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.repository.ClienteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteHistoricoService clienteHistoricoService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ClienteDto> findAll() {
        return clienteRepository.findByDeletedAtIsNull().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteDto findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        
        if (cliente.getDeletedAt() != null) {
            throw new RuntimeException("Cliente eliminado (soft delete)");
        }
        
        return convertToDto(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteDto findByRef(String ref) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(ref)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + ref));
        return convertToDto(cliente);
    }

    @Transactional
    public ClienteDto create(ClienteDto dto) {
        // Validar que no existe otro cliente con mismo CIF
        if (clienteRepository.existsByCif(dto.getCif())) {
            throw new RuntimeException("Ya existe un cliente con el CIF: " + dto.getCif());
        }

        // Validar que no existe otro cliente con mismo REF
        if (clienteRepository.existsByRef(dto.getRef())) {
            throw new RuntimeException("Ya existe un cliente con la referencia: " + dto.getRef());
        }

        Cliente cliente = Cliente.builder()
                .ref(dto.getRef())
                .nombre(dto.getNombre())
                .cif(dto.getCif())
                .direccion(dto.getDireccion())
                .cp(dto.getCp())
                .tarifa(dto.getTarifa())
                .build();

        Cliente savedCliente = clienteRepository.save(cliente);
        
        // Registrar en histórico
        clienteHistoricoService.registrarCambio(savedCliente, "INSERT", null, null);
        
        log.info("Cliente creado: {}", savedCliente.getRef());
        return convertToDto(savedCliente);
    }

    @Transactional
    public ClienteDto update(Long id, ClienteDto dto) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        if (clienteExistente.getDeletedAt() != null) {
            throw new RuntimeException("No se puede actualizar un cliente eliminado");
        }

        // Guardar estado anterior para el histórico
        Map<String, Object> cambiosAnteriores = buildCambiosMap(clienteExistente);

        // Validar unicidad de CIF (solo si cambió)
        if (!clienteExistente.getCif().equals(dto.getCif())) {
            if (clienteRepository.existsByCif(dto.getCif())) {
                throw new RuntimeException("Ya existe un cliente con el CIF: " + dto.getCif());
            }
        }

        // Validar unicidad de REF (solo si cambió)
        if (!clienteExistente.getRef().equals(dto.getRef())) {
            if (clienteRepository.existsByRef(dto.getRef())) {
                throw new RuntimeException("Ya existe un cliente con la referencia: " + dto.getRef());
            }
        }

        // Actualizar campos
        clienteExistente.setRef(dto.getRef());
        clienteExistente.setNombre(dto.getNombre());
        clienteExistente.setCif(dto.getCif());
        clienteExistente.setDireccion(dto.getDireccion());
        clienteExistente.setCp(dto.getCp());
        clienteExistente.setTarifa(dto.getTarifa());

        Cliente updatedCliente = clienteRepository.save(clienteExistente);

        // Registrar cambios en histórico
        Map<String, Object> cambiosNuevos = buildCambiosMap(updatedCliente);
        clienteHistoricoService.registrarCambio(updatedCliente, "UPDATE", cambiosAnteriores, cambiosNuevos);

        log.info("Cliente actualizado: {}", updatedCliente.getRef());
        return convertToDto(updatedCliente);
    }

    @Transactional
    public void delete(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        if (cliente.getDeletedAt() != null) {
            throw new RuntimeException("Cliente ya eliminado");
        }

        // Soft delete
        cliente.setDeletedAt(LocalDateTime.now());
        clienteRepository.save(cliente);

        // Registrar en histórico
        clienteHistoricoService.registrarCambio(cliente, "DELETE", null, null);

        log.info("Cliente eliminado (soft delete): {}", cliente.getRef());
    }

    @Transactional
    public void restore(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        if (cliente.getDeletedAt() == null) {
            throw new RuntimeException("Cliente no está eliminado");
        }

        cliente.setDeletedAt(null);
        clienteRepository.save(cliente);

        log.info("Cliente restaurado: {}", cliente.getRef());
    }

    private ClienteDto convertToDto(Cliente cliente) {
        return ClienteDto.builder()
                .id(cliente.getId())
                .ref(cliente.getRef())
                .nombre(cliente.getNombre())
                .cif(cliente.getCif())
                .direccion(cliente.getDireccion())
                .cp(cliente.getCp())
                .tarifa(cliente.getTarifa())
                .createdAt(cliente.getCreatedAt())
                .updatedAt(cliente.getUpdatedAt())
                .deletedAt(cliente.getDeletedAt())
                .build();
    }

    private Map<String, Object> buildCambiosMap(Cliente cliente) {
        Map<String, Object> cambios = new HashMap<>();
        cambios.put("ref", cliente.getRef());
        cambios.put("nombre", cliente.getNombre());
        cambios.put("cif", cliente.getCif());
        cambios.put("direccion", cliente.getDireccion());
        cambios.put("cp", cliente.getCp());
        cambios.put("tarifa", cliente.getTarifa());
        return cambios;
    }
}
