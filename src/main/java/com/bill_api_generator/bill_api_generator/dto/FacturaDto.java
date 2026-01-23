package com.bill_api_generator.bill_api_generator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacturaDto {
    
    private Long id;
    private String numeroFactura;
    private Integer horas;
    private LocalDate fecha;
    private String mesFact;
    
    // Datos del cliente
    private String nombreCliente;
    private String cifCliente;
    private String direccionCliente;
    
    // Datos del emisor (fijos)
    private String emisorNombre;
    private String emisorDireccion;
    private String emisorCp;
    private String emisorNif;
    private String emisorIban;
    
    // Cálculos
    private BigDecimal imponible;
    private BigDecimal irpf;
    private BigDecimal iva;
    private BigDecimal total;
}
