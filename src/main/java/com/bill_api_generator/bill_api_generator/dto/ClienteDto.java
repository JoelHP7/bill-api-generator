package com.bill_api_generator.bill_api_generator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteDto {
    
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    
    @NotBlank(message = "El CIF es obligatorio")
    private String cif;
    
    @NotBlank(message = "La dirección es obligatoria")
    private String direccion;
    
    private String cp;
    
    @NotNull(message = "La tarifa es obligatoria")
    @Positive(message = "La tarifa debe ser mayor que cero")
    private BigDecimal tarifa;
}
