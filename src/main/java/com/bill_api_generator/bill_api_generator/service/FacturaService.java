package com.bill_api_generator.bill_api_generator.service;

import com.bill_api_generator.bill_api_generator.dto.FacturaDto;
import com.bill_api_generator.bill_api_generator.model.Cliente;
import com.bill_api_generator.bill_api_generator.model.Factura;
import com.bill_api_generator.bill_api_generator.repository.ClienteRepository;
import com.bill_api_generator.bill_api_generator.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FacturaService {

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final DocumentGeneratorService documentGeneratorService;

    // Constantes para los porcentajes
    private static final BigDecimal PORCENTAJE_IRPF = new BigDecimal("0.15"); // 15%
    private static final BigDecimal PORCENTAJE_IVA = new BigDecimal("0.21"); // 21%

    // Datos del emisor (fijos) - PERSONALIZA AQUÍ
    private static final String EMISOR_NOMBRE = "JOEL HERNANDEZ PLA";
    private static final String EMISOR_DIRECCION = "Carrer Mossen Ernest Mateu, 7";
    private static final String EMISOR_CP = "08181";
    private static final String EMISOR_NIF = "47818505X";
    private static final String EMISOR_IBAN = "ES62 1465 0180 71 1734028810";

    @Transactional
    public FacturaDto generateFactura(Long clienteId, Integer horas, LocalDate fecha) {
        // Obtener el cliente
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + clienteId));

        // Si no se proporciona fecha, usar la fecha actual
        if (fecha == null) {
            fecha = LocalDate.now();
        }

        // Generar número de factura (formato: YYYY-NNNN)
        String numeroFactura = generateNumeroFactura(fecha);

        // Calcular montos
        BigDecimal imponible = cliente.getTarifa().multiply(new BigDecimal(horas))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal irpf = imponible.multiply(PORCENTAJE_IRPF)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal iva = imponible.multiply(PORCENTAJE_IVA)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = imponible.subtract(irpf).add(iva)
                .setScale(2, RoundingMode.HALF_UP);

        // Crear y guardar la factura
        Factura factura = Factura.builder()
                .numeroFactura(numeroFactura)
                .horas(horas)
                .cliente(cliente)
                .fecha(fecha)
                .build();

        Factura savedFactura = facturaRepository.save(factura);

        // Crear el DTO con todos los datos
        return FacturaDto.builder()
                .id(savedFactura.getId())
                .numeroFactura(numeroFactura)
                .horas(horas)
                .fecha(fecha)
                .mesFact(getMesFactura(fecha))
                // Datos del cliente
                .nombreCliente(cliente.getNombre())
                .cifCliente(cliente.getCif())
                .direccionCliente(cliente.getDireccion())
                // Datos del emisor
                .emisorNombre(EMISOR_NOMBRE)
                .emisorDireccion(EMISOR_DIRECCION)
                .emisorCp(EMISOR_CP)
                .emisorNif(EMISOR_NIF)
                .emisorIban(EMISOR_IBAN)
                // Cálculos
                .imponible(imponible)
                .irpf(irpf)
                .iva(iva)
                .total(total)
                .build();
    }

    /**
     * Genera una factura Y su documento DOCX
     * @param clienteId ID del cliente
     * @param horas Horas trabajadas
     * @param fecha Fecha de la factura (null = fecha actual)
     * @return ByteArrayOutputStream con el documento DOCX generado
     * @throws IOException si hay error al generar el documento
     */
    @Transactional
    public ByteArrayOutputStream generateFacturaWithDocument(Long clienteId, Integer horas, LocalDate fecha) throws IOException {
        // 1. Generar la factura y guardarla en BD
        FacturaDto facturaDto = generateFactura(clienteId, horas, fecha);

        // 2. Generar el documento DOCX con los datos
        return documentGeneratorService.generateFacturaDocx(facturaDto);
    }

    private String generateNumeroFactura(LocalDate fecha) {
        Long maxId = facturaRepository.findMaxId();
        Long nextId = (maxId != null ? maxId : 0L) + 1;
        return String.format("%d-%04d", fecha.getYear(), nextId);
    }

    private String getMesFactura(LocalDate fecha) {
        // Obtener el nombre del mes en español
        String mes = fecha.getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        // Capitalizar la primera letra
        return mes.substring(0, 1).toUpperCase() + mes.substring(1);
    }

    @Transactional(readOnly = true)
    public FacturaDto findById(Long id) {
        Factura factura = facturaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Factura no encontrada con id: " + id));
        return convertToDto(factura);
    }

    private FacturaDto convertToDto(Factura factura) {
        Cliente cliente = factura.getCliente();

        // Recalcular los montos
        BigDecimal imponible = cliente.getTarifa().multiply(new BigDecimal(factura.getHoras()))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal irpf = imponible.multiply(PORCENTAJE_IRPF)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal iva = imponible.multiply(PORCENTAJE_IVA)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal total = imponible.subtract(irpf).add(iva)
                .setScale(2, RoundingMode.HALF_UP);

        return FacturaDto.builder()
                .id(factura.getId())
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
