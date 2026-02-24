package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.ClientDto;
import com.bill_api_generator.bill_api_generator.exception.ClientDeletedException;
import com.bill_api_generator.bill_api_generator.exception.ClientNotFoundException;
import com.bill_api_generator.bill_api_generator.exception.ClientNotDeletedException;
import com.bill_api_generator.bill_api_generator.exception.DuplicateClientRefException;
import com.bill_api_generator.bill_api_generator.exception.DuplicateTaxIdException;
import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.repository.ClientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service class for managing client operations.
 * Handles CRUD operations, validation, and audit trail.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientHistoryService clientHistoryService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<ClientDto> findAll() {
        return clientRepository.findByDeletedAtIsNull().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientDto findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (client.getDeletedAt() != null) {
            throw new ClientDeletedException(id);
        }
        
        return convertToDto(client);
    }

    @Transactional(readOnly = true)
    public ClientDto findByRef(String ref) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(ref)
                .orElseThrow(() -> new ClientNotFoundException(ref));
        return convertToDto(client);
    }

    @Transactional
    public ClientDto create(ClientDto dto) {
        // Validate that no other client exists with the same tax ID
        if (clientRepository.existsByTaxId(dto.getTaxId())) {
            throw new DuplicateTaxIdException(dto.getTaxId());
        }

        // Validate that no other client exists with the same reference
        if (clientRepository.existsByRef(dto.getRef())) {
            throw new DuplicateClientRefException(dto.getRef());
        }

        Client client = Client.builder()
                .ref(dto.getRef())
                .name(dto.getName())
                .taxId(dto.getTaxId())
                .address(dto.getAddress())
                .postalCode(dto.getPostalCode())
                .rate(dto.getRate())
                .build();

        Client savedClient = clientRepository.save(client);
        
        // Register in history
        clientHistoryService.registerChange(savedClient, "INSERT", null, null);
        
        log.info("Client created: {}", savedClient.getRef());
        return convertToDto(savedClient);
    }

    @Transactional
    public ClientDto update(Long id, ClientDto dto) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (existingClient.getDeletedAt() != null) {
            throw new ClientDeletedException(id);
        }

        // Save previous state for history
        Map<String, Object> previousChanges = buildChangesMap(existingClient);

        // Validate tax ID uniqueness (only if changed)
        if (!existingClient.getTaxId().equals(dto.getTaxId())) {
            if (clientRepository.existsByTaxId(dto.getTaxId())) {
                throw new DuplicateTaxIdException(dto.getTaxId());
            }
        }

        // Validate reference uniqueness (only if changed)
        if (!existingClient.getRef().equals(dto.getRef())) {
            if (clientRepository.existsByRef(dto.getRef())) {
                throw new DuplicateClientRefException(dto.getRef());
            }
        }

        // Update fields
        existingClient.setRef(dto.getRef());
        existingClient.setName(dto.getName());
        existingClient.setTaxId(dto.getTaxId());
        existingClient.setAddress(dto.getAddress());
        existingClient.setPostalCode(dto.getPostalCode());
        existingClient.setRate(dto.getRate());

        Client updatedClient = clientRepository.save(existingClient);

        // Register changes in history
        Map<String, Object> newChanges = buildChangesMap(updatedClient);
        clientHistoryService.registerChange(updatedClient, "UPDATE", previousChanges, newChanges);

        log.info("Client updated: {}", updatedClient.getRef());
        return convertToDto(updatedClient);
    }

    @Transactional
    public void delete(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (client.getDeletedAt() != null) {
            throw new ClientDeletedException(id);
        }

        // Soft delete
        client.setDeletedAt(LocalDateTime.now());
        clientRepository.save(client);

        // Register in history
        clientHistoryService.registerChange(client, "DELETE", null, null);

        log.info("Client deleted (soft delete): {}", client.getRef());
    }

    @Transactional
    public void restore(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        if (client.getDeletedAt() == null) {
            throw new ClientNotDeletedException(id);
        }

        client.setDeletedAt(null);
        clientRepository.save(client);

        log.info("Client restored: {}", client.getRef());
    }

    private ClientDto convertToDto(Client client) {
        return ClientDto.builder()
                .id(client.getId())
                .ref(client.getRef())
                .name(client.getName())
                .taxId(client.getTaxId())
                .address(client.getAddress())
                .postalCode(client.getPostalCode())
                .rate(client.getRate())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .deletedAt(client.getDeletedAt())
                .build();
    }

    private Map<String, Object> buildChangesMap(Client client) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("ref", client.getRef());
        changes.put("name", client.getName());
        changes.put("taxId", client.getTaxId());
        changes.put("address", client.getAddress());
        changes.put("postalCode", client.getPostalCode());
        changes.put("rate", client.getRate());
        return changes;
    }
}
