package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String nombre;         // p.ej. "AXPE CONSULTING, S.L."
    
    @Column(nullable = false, unique = true)
    private String cif;            // p.ej. "B84184548"
    
    @Column(nullable = false)
    private String direccion;      // p.ej. "Calle Arturo Soria 122, Madrid"
    
    private String cp;             // p.ej. "08204"
    
    @Column(nullable = false)
    private BigDecimal tarifa;     // Tarifa por hora
}
