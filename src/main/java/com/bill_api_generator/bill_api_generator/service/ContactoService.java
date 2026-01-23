package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.ContactoDto;
import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.Contacto;
import com.bill_api_generator.bill_api_generator.repository.ClienteRepository;
import com.bill_api_generator.bill_api_generator.repository.ContactoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactoService {

    private final ContactoRepository contactoRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ContactoDto> findByClienteRef(String clienteRef) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(clienteRef)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + clienteRef));
        
        return contactoRepository.findByClienteIdAndDeletedAtIsNull(cliente.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContactoDto findById(Long id) {
        Contacto contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con id: " + id));
        
        if (contacto.getDeletedAt() != null) {
            throw new RuntimeException("Contacto eliminado (soft delete)");
        }
        
        return convertToDto(contacto);
    }

    @Transactional(readOnly = true)
    public Optional<ContactoDto> findContactoPrincipal(String clienteRef) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(clienteRef)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + clienteRef));
        
        return contactoRepository.findByClienteIdAndPrincipalTrueAndDeletedAtIsNull(cliente.getId())
                .map(this::convertToDto);
    }

    @Transactional
    public ContactoDto create(String clienteRef, ContactoDto dto) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(clienteRef)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + clienteRef));

        // Si es principal, desmarcar otros contactos principales
        if (Boolean.TRUE.equals(dto.getPrincipal())) {
            desmarcarContactosPrincipales(cliente.getId());
        }

        Contacto contacto = Contacto.builder()
                .cliente(cliente)
                .nombre(dto.getNombre())
                .email(dto.getEmail())
                .telefono(dto.getTelefono())
                .cargo(dto.getCargo())
                .principal(dto.getPrincipal() != null ? dto.getPrincipal() : false)
                .build();

        Contacto savedContacto = contactoRepository.save(contacto);
        log.info("Contacto creado: {} para cliente: {}", savedContacto.getNombre(), clienteRef);
        
        return convertToDto(savedContacto);
    }

    @Transactional
    public ContactoDto update(Long id, ContactoDto dto) {
        Contacto contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con id: " + id));

        if (contacto.getDeletedAt() != null) {
            throw new RuntimeException("No se puede actualizar un contacto eliminado");
        }

        // Si se marca como principal, desmarcar otros
        if (Boolean.TRUE.equals(dto.getPrincipal()) && !Boolean.TRUE.equals(contacto.getPrincipal())) {
            desmarcarContactosPrincipales(contacto.getCliente().getId());
        }

        contacto.setNombre(dto.getNombre());
        contacto.setEmail(dto.getEmail());
        contacto.setTelefono(dto.getTelefono());
        contacto.setCargo(dto.getCargo());
        contacto.setPrincipal(dto.getPrincipal() != null ? dto.getPrincipal() : false);

        Contacto updatedContacto = contactoRepository.save(contacto);
        log.info("Contacto actualizado: {}", updatedContacto.getNombre());
        
        return convertToDto(updatedContacto);
    }

    @Transactional
    public void delete(Long id) {
        Contacto contacto = contactoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con id: " + id));

        if (contacto.getDeletedAt() != null) {
            throw new RuntimeException("Contacto ya eliminado");
        }

        // Soft delete
        contacto.setDeletedAt(LocalDateTime.now());
        contactoRepository.save(contacto);

        log.info("Contacto eliminado (soft delete): {}", contacto.getNombre());
    }

    private void desmarcarContactosPrincipales(Long clienteId) {
        List<Contacto> contactos = contactoRepository.findByClienteId(clienteId);
        for (Contacto c : contactos) {
            if (Boolean.TRUE.equals(c.getPrincipal())) {
                c.setPrincipal(false);
                contactoRepository.save(c);
            }
        }
    }

    private ContactoDto convertToDto(Contacto contacto) {
        return ContactoDto.builder()
                .id(contacto.getId())
                .clienteId(contacto.getCliente().getId())
                .nombre(contacto.getNombre())
                .email(contacto.getEmail())
                .telefono(contacto.getTelefono())
                .cargo(contacto.getCargo())
                .principal(contacto.getPrincipal())
                .createdAt(contacto.getCreatedAt())
                .updatedAt(contacto.getUpdatedAt())
                .deletedAt(contacto.getDeletedAt())
                .build();
    }
}
