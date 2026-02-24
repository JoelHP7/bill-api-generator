package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.ContactDto;
import com.bill_api_generator.bill_api_generator.exception.ClientNotFoundException;
import com.bill_api_generator.bill_api_generator.exception.ContactDeletedException;
import com.bill_api_generator.bill_api_generator.exception.ContactNotFoundException;
import com.bill_api_generator.bill_api_generator.model.Client;
import com.bill_api_generator.bill_api_generator.model.Contact;
import com.bill_api_generator.bill_api_generator.repository.ClientRepository;
import com.bill_api_generator.bill_api_generator.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing contact operations.
 * Handles CRUD operations for contacts associated with clients.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository contactRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ContactDto> findByClientRef(String clientRef) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));

        return contactRepository.findByClientIdAndDeletedAtIsNull(client.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactDto findById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        if (contact.getDeletedAt() != null) {
            throw new ContactDeletedException(contact.getId());
        }
        
        return convertToDto(contact);
    }

    @Transactional(readOnly = true)
    public Optional<ContactDto> findPrimaryContact(String clientRef) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));

        return contactRepository.findByClientIdAndIsPrimaryTrueAndDeletedAtIsNull(client.getId())
                .map(this::convertToDto);
    }

    @Transactional
    public ContactDto create(String clientRef, ContactDto dto) {
        Client client = clientRepository.findByRefAndDeletedAtIsNull(clientRef)
                .orElseThrow(() -> new ClientNotFoundException(clientRef));

        // If marked as primary, unmark other primary contacts
        if (Boolean.TRUE.equals(dto.getIsPrimary())) {
            unmarkPrimaryContacts(client.getId());
        }

        Contact contact = Contact.builder()
                .client(client)
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .position(dto.getPosition())
                .isPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false)
                .build();

        Contact savedContact = contactRepository.save(contact);
        log.info("Contact created: {} for client: {}", savedContact.getName(), clientRef);
        
        return convertToDto(savedContact);
    }

    @Transactional
    public ContactDto update(Long id, ContactDto dto) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        if (contact.getDeletedAt() != null) {
            throw new ContactDeletedException(contact.getId());
        }

        // If marking as primary, unmark others
        if (Boolean.TRUE.equals(dto.getIsPrimary()) && !Boolean.TRUE.equals(contact.getIsPrimary())) {
            unmarkPrimaryContacts(contact.getClient().getId());
        }

        contact.setName(dto.getName());
        contact.setEmail(dto.getEmail());
        contact.setPhone(dto.getPhone());
        contact.setPosition(dto.getPosition());
        contact.setIsPrimary(dto.getIsPrimary() != null ? dto.getIsPrimary() : false);

        Contact updatedContact = contactRepository.save(contact);
        log.info("Contact updated: {}", updatedContact.getName());
        
        return convertToDto(updatedContact);
    }

    @Transactional
    public void delete(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        if (contact.getDeletedAt() != null) {
            throw new ContactDeletedException(contact.getId());
        }

        // Soft delete
        contact.setDeletedAt(LocalDateTime.now());
        contactRepository.save(contact);

        log.info("Contact deleted (soft delete): {}", contact.getName());
    }

    private void unmarkPrimaryContacts(Long clientId) {
        List<Contact> contacts = contactRepository.findByClientId(clientId);
        for (Contact c : contacts) {
            if (Boolean.TRUE.equals(c.getIsPrimary())) {
                c.setIsPrimary(false);
                contactRepository.save(c);
            }
        }
    }

    private ContactDto convertToDto(Contact contact) {
        return ContactDto.builder()
                .id(contact.getId())
                .clientId(contact.getClient().getId())
                .name(contact.getName())
                .email(contact.getEmail())
                .phone(contact.getPhone())
                .position(contact.getPosition())
                .isPrimary(contact.getIsPrimary())
                .createdAt(contact.getCreatedAt())
                .updatedAt(contact.getUpdatedAt())
                .deletedAt(contact.getDeletedAt())
                .build();
    }
}
