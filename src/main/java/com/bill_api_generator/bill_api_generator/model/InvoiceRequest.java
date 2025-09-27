package com.bill_api_generator.bill_api_generator.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceRequest {
    // Emisor
    private String emisorNombre;       // "JOEL HERNANDEZ PLA"
    private String emisorDireccion;    // "Carrer Mossen Ernest Mateu, 7"
    private String emisorCp;           // "08181"
    private String emisorNif;          // "47818505X"

    // IBAN condicionado
    private boolean mostrarIban;       // si true, se imprime la línea del IBAN
    private String iban;               // "ES62 1465 0180 71 1734028810"

    // Cabecera de factura
    private String numeroFactura;      // "52"
    private LocalDate fecha;           // 2025-09-25

    // Destinatarios ("Para" -> pueden ser varios)
    private List<Cliente> para;        // atributos del objeto Cliente

    // Cuerpo
    private String descripcionLinea;   // "Trabajos profesionales Septiembre 2025 – 152 horas"
    private double importeLinea;       // 6080.00

    // Impuestos/retención
    private double porcentajeIrpf;     // 7.0
    private double porcentajeIva;      // 21.0
}
