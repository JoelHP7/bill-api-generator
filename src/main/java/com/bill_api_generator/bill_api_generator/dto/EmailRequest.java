package com.bill_api_generator.bill_api_generator.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {
    
    @NotNull(message = "El ID de la factura es obligatorio")
    private Long facturaId;
    
    @NotBlank(message = "El email del destinatario es obligatorio")
    @Email(message = "Email inválido")
    private String to;
    
    private List<@Email String> cc;  // Copia
    
    private String asunto;  // Si es null, se genera automáticamente
    
    private String mensaje;  // Mensaje adicional personalizado
}
