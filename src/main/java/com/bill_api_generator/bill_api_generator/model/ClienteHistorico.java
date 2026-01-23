package com.bill_api_generator.bill_api_generator.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cliente_historico")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteHistorico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long clienteId;  // ID del cliente modificado
    
    @Column(nullable = false, length = 50)
    private String ref;
    
    private String nombre;
    
    private String cif;
    
    private String direccion;
    
    private String cp;
    
    private BigDecimal tarifa;
    
    @Column(nullable = false, length = 20)
    private String operacion;  // INSERT, UPDATE, DELETE
    
    @Column(nullable = false)
    private LocalDateTime fechaCambio;
    
    private String usuarioModificacion;  // Para futuros casos con autenticación
    
    @Column(columnDefinition = "TEXT")
    private String detallesCambio;  // JSON con los campos modificados
}
