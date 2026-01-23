package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "facturas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "factura_numero_seq")
    @SequenceGenerator(
        name = "factura_numero_seq",
        sequenceName = "factura_numero_secuencial",
        initialValue = 70,
        allocationSize = 1
    )
    @Column(name = "numero_secuencial")
    private Long numeroSecuencial;  // Número secuencial global (70, 71, 72...)
    
    @Column(nullable = false, unique = true)
    private String numeroFactura;  // Formato: 2026-0070, 2026-0071...
    
    @Column(nullable = false)
    private Integer horas;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
    
    @Column(nullable = false)
    private LocalDate fecha;
}
