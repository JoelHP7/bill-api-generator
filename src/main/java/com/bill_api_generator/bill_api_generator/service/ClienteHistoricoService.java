package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.ClienteHistorico;
import com.bill_api_generator.bill_api_generator.repository.ClienteHistoricoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClienteHistoricoService {

    private final ClienteHistoricoRepository clienteHistoricoRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registra un cambio en el histórico de un cliente
     * Se ejecuta en una transacción independiente para asegurar que se guarde incluso si falla la operación principal
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCambio(Cliente cliente, String operacion, 
                                 Map<String, Object> valoresAnteriores, 
                                 Map<String, Object> valoresNuevos) {
        try {
            String detallesCambio = null;

            if ("UPDATE".equals(operacion) && valoresAnteriores != null && valoresNuevos != null) {
                Map<String, Map<String, Object>> cambios = new HashMap<>();
                
                for (String campo : valoresAnteriores.keySet()) {
                    Object valorAnterior = valoresAnteriores.get(campo);
                    Object valorNuevo = valoresNuevos.get(campo);
                    
                    if (!String.valueOf(valorAnterior).equals(String.valueOf(valorNuevo))) {
                        Map<String, Object> cambio = new HashMap<>();
                        cambio.put("anterior", valorAnterior);
                        cambio.put("nuevo", valorNuevo);
                        cambios.put(campo, cambio);
                    }
                }
                
                if (!cambios.isEmpty()) {
                    detallesCambio = objectMapper.writeValueAsString(cambios);
                }
            }

            ClienteHistorico historico = ClienteHistorico.builder()
                    .clienteId(cliente.getId())
                    .ref(cliente.getRef())
                    .nombre(cliente.getNombre())
                    .cif(cliente.getCif())
                    .direccion(cliente.getDireccion())
                    .cp(cliente.getCp())
                    .tarifa(cliente.getTarifa())
                    .operacion(operacion)
                    .fechaCambio(LocalDateTime.now())
                    .detallesCambio(detallesCambio)
                    .build();

            clienteHistoricoRepository.save(historico);
            log.info("Cambio registrado en histórico: Cliente ID={}, Operación={}", cliente.getId(), operacion);

        } catch (Exception e) {
            log.error("Error al registrar cambio en histórico para cliente ID={}", cliente.getId(), e);
            // No lanzar excepción para no afectar la transacción principal
        }
    }

    @Transactional(readOnly = true)
    public List<ClienteHistorico> getHistorialCliente(Long clienteId) {
        return clienteHistoricoRepository.findByClienteIdOrderByFechaCambioDesc(clienteId);
    }

    @Transactional(readOnly = true)
    public List<ClienteHistorico> getHistorialPorOperacion(String operacion) {
        return clienteHistoricoRepository.findByOperacion(operacion);
    }
}
