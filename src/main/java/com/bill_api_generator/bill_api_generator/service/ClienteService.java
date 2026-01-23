package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.ClienteDto;
import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    public List<ClienteDto> findAll() {
        return clienteRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteDto findById(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
        return convertToDto(cliente);
    }

    @Transactional
    public ClienteDto create(ClienteDto clienteDto) {
        if (clienteRepository.existsByCif(clienteDto.getCif())) {
            throw new RuntimeException("Ya existe un cliente con el CIF: " + clienteDto.getCif());
        }

        Cliente cliente = convertToEntity(clienteDto);
        Cliente savedCliente = clienteRepository.save(cliente);
        return convertToDto(savedCliente);
    }

    @Transactional
    public ClienteDto update(Long id, ClienteDto clienteDto) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));

        // Verificar que el CIF no esté en uso por otro cliente
        if (!cliente.getCif().equals(clienteDto.getCif()) &&
            clienteRepository.existsByCif(clienteDto.getCif())) {
            throw new RuntimeException("Ya existe un cliente con el CIF: " + clienteDto.getCif());
        }

        cliente.setNombre(clienteDto.getNombre());
        cliente.setCif(clienteDto.getCif());
        cliente.setDireccion(clienteDto.getDireccion());
        cliente.setCp(clienteDto.getCp());
        cliente.setTarifa(clienteDto.getTarifa());

        Cliente updatedCliente = clienteRepository.save(cliente);
        return convertToDto(updatedCliente);
    }

    @Transactional
    public void delete(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
    }

    private ClienteDto convertToDto(Cliente cliente) {
        return ClienteDto.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .cif(cliente.getCif())
                .direccion(cliente.getDireccion())
                .cp(cliente.getCp())
                .tarifa(cliente.getTarifa())
                .build();
    }

    private Cliente convertToEntity(ClienteDto dto) {
        return Cliente.builder()
                .nombre(dto.getNombre())
                .cif(dto.getCif())
                .direccion(dto.getDireccion())
                .cp(dto.getCp())
                .tarifa(dto.getTarifa())
                .build();
    }
}
