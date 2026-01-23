package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.FacturaDto;
import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.Factura;
import com.bill_api_generator.bill_api_generator.repository.ClienteRepository;
import com.bill_api_generator.bill_api_generator.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final DocumentGeneratorService documentGeneratorService;

    // Constantes para los porcentajes
    private static final BigDecimal PORCENTAJE_IRPF = new BigDecimal("0.15"); // 15%
    private static final BigDecimal PORCENTAJE_IVA = new BigDecimal("0.21"); // 21%

    // Datos del emisor (fijos)
    private static final String EMISOR_NOMBRE = "JOEL HERNANDEZ PLA";
    private static final String EMISOR_DIRECCION = "Carrer Mossen Ernest Mateu, 7";
    private static final String EMISOR_CP = "08181";
    private static final String EMISOR_NIF = "47818505X";
    private static final String EMISOR_IBAN = "ES62 1465 0180 71 1734028810";

    /**
     * Genera una factura usando el REF del cliente (nuevo método principal)
     */
    @Transactional
    public Map<String, Object> generateFacturaByRef(String clienteRef, Integer horas, LocalDate fecha) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(clienteRef)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + clienteRef));
        return generateFacturaInternal(cliente, horas, fecha);
    }

    /**
     * Genera una factura usando el ID del cliente (mantener compatibilidad)
     */
    @Transactional
    public Map<String, Object> generateFactura(Long clienteId, Integer horas, LocalDate fecha) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + clienteId));
        return generateFacturaInternal(cliente, horas, fecha);
    }

    /**
     * Método interno que genera la factura con verificación de duplicados
     */
    private Map<String, Object> generateFacturaInternal(Cliente cliente, Integer horas, LocalDate fecha) {
        Map<String, Object> response = new HashMap<>();

        if (fecha == null) {
            fecha = LocalDate.now();
        }

        // VERIFICAR DUPLICADOS
        Optional<Factura> facturaExistente = facturaRepository.findByClienteIdAndFechaAndHoras(
                cliente.getId(), fecha, horas);

        if (facturaExistente.isPresent()) {
            log.warn("Factura duplicada detectada para cliente: {}, fecha: {}, horas: {}",
                    cliente.getRef(), fecha, horas);

            response.put("status", "DUPLICADA");
            response.put("mensaje", "Ya existe una factura con estos parámetros");
            response.put("factura", convertToDto(facturaExistente.get()));
            response.put("sugerencias", Arrays.asList(
                    "Verificar si la fecha es correcta",
                    "Verificar si las horas son correctas",
                    "Si necesitas modificarla, usa PUT /api/facturas/{id}",
                    "Si es correcta, puedes descargar el documento existente"
            ));
            return response;
        }

        // GENERAR NUEVA FACTURA
        log.info("Generando nueva factura para cliente: {}, fecha: {}, horas: {}",
                cliente.getRef(), fecha, horas);

        String numeroFactura = generateNumeroFactura(fecha);

        Factura factura = Factura.builder()
                .numeroFactura(numeroFactura)
                .horas(horas)
                .cliente(cliente)
                .fecha(fecha)
                .build();

        Factura savedFactura = facturaRepository.save(factura);

        // Actualizar numeroFactura con secuencial real
        savedFactura.setNumeroFactura(generateNumeroFacturaConSecuencial(savedFactura.getNumeroSecuencial()));
        savedFactura = facturaRepository.save(savedFactura);

        log.info("Factura generada: {}", savedFactura.getNumeroFactura());

        response.put("status", "CREADA");
        response.put("mensaje", "Factura generada exitosamente");
        response.put("factura", convertToDto(savedFactura));
        return response;
    }

    @Transactional
    public ByteArrayOutputStream generateFacturaWithDocumentByRef(String clienteRef, Integer horas, LocalDate fecha) throws IOException {
        Map<String, Object> response = generateFacturaByRef(clienteRef, horas, fecha);
        FacturaDto facturaDto = (FacturaDto) response.get("factura");
        return documentGeneratorService.generateFacturaDocx(facturaDto);
    }

    @Transactional
    public ByteArrayOutputStream generateFacturaWithDocument(Long clienteId, Integer horas, LocalDate fecha) throws IOException {
        Map<String, Object> response = generateFactura(clienteId, horas, fecha);
        FacturaDto facturaDto = (FacturaDto) response.get("factura");
        return documentGeneratorService.generateFacturaDocx(facturaDto);
    }

    private String generateNumeroFactura(LocalDate fecha) {
        return String.format("%d-TEMP", fecha.getYear());
    }

    private String generateNumeroFacturaConSecuencial(Long secuencial) {
        return secuencial.toString();
    }

    private String getMesFactura(LocalDate fecha) {
        String mes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        mes = mes.substring(0, 1).toUpperCase() + mes.substring(1);
        return mes + " " + fecha.getYear();
    }

    @Transactional(readOnly = true)
    public FacturaDto findById(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con id: " + id));
        return convertToDto(factura);
    }

    @Transactional(readOnly = true)
    public List<FacturaDto> findByClienteRef(String clienteRef) {
        Cliente cliente = clienteRepository.findByRefAndDeletedAtIsNull(clienteRef)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ref: " + clienteRef));
        return facturaRepository.findByClienteId(cliente.getId()).stream()
                .map(this::convertToDto)
                .toList();
    }

    private FacturaDto convertToDto(Factura factura) {
        Cliente cliente = factura.getCliente();
        BigDecimal imponible = cliente.getTarifa().multiply(new BigDecimal(factura.getHoras()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal irpf = imponible.multiply(PORCENTAJE_IRPF).setScale(2, RoundingMode.HALF_UP);
        BigDecimal iva = imponible.multiply(PORCENTAJE_IVA).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = imponible.subtract(irpf).add(iva).setScale(2, RoundingMode.HALF_UP);

        return FacturaDto.builder()
                .id(factura.getNumeroSecuencial())
                .numeroFactura(factura.getNumeroFactura())
                .horas(factura.getHoras())
                .fecha(factura.getFecha())
                .mesFact(getMesFactura(factura.getFecha()))
                .nombreCliente(cliente.getNombre())
                .cifCliente(cliente.getCif())
                .direccionCliente(cliente.getDireccion())
                .emisorNombre(EMISOR_NOMBRE)
                .emisorDireccion(EMISOR_DIRECCION)
                .emisorCp(EMISOR_CP)
                .emisorNif(EMISOR_NIF)
                .emisorIban(EMISOR_IBAN)
                .imponible(imponible)
                .irpf(irpf)
                .iva(iva)
                .total(total)
                .build();
    }
}
