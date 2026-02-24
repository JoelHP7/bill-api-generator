package com.bill_api_generator.bill_api_generator.controller;

import com.bill_api_generator.bill_api_generator.dto.ContactDto;
import com.bill_api_generator.bill_api_generator.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing contacts.
 * Provides CRUD operations for contact entities associated with clients.
 */
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /**
     * Get all contacts for a client.
     * GET /api/contacts/client/{clientRef}
     */
    @GetMapping("/client/{clientRef}")
    public ResponseEntity<List<ContactDto>> getContactsByClient(@PathVariable String clientRef) {
        List<ContactDto> contacts = contactService.findByClientRef(clientRef);
        return ResponseEntity.ok(contacts);
    }

    /**
     * Get the primary contact for a client.
     * GET /api/contacts/client/{clientRef}/primary
     */
    @GetMapping("/client/{clientRef}/primary")
    public ResponseEntity<ContactDto> getPrimaryContact(@PathVariable String clientRef) {
        return contactService.findPrimaryContact(clientRef)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get a contact by ID.
     * GET /api/contacts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContactDto> getContactById(@PathVariable Long id) {
        ContactDto contact = contactService.findById(id);
        return ResponseEntity.ok(contact);
    }

    /**
     * Create a contact for a client.
     * POST /api/contacts/client/{clientRef}
     */
    @PostMapping("/client/{clientRef}")
    public ResponseEntity<ContactDto> createContact(
            @PathVariable String clientRef,
            @Valid @RequestBody ContactDto dto) {
        ContactDto createdContact = contactService.create(clientRef, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContact);
    }

    /**
     * Update an existing contact.
     * PUT /api/contacts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ContactDto> updateContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactDto dto) {
        ContactDto updatedContact = contactService.update(id, dto);
        return ResponseEntity.ok(updatedContact);
    }

    /**
     * Delete a contact (soft delete).
     * DELETE /api/contacts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
