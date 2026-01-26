package com.bill_api_generator.bill_api_generator.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Client entity.
 * Used for API requests and responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientDto {
    
    private Long id;
    
    @NotBlank(message = "Reference is required")
    private String ref;
    
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Tax ID is required")
    private String taxId;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    private String postalCode;
    
    @NotNull(message = "Rate is required")
    @Positive(message = "Rate must be greater than zero")
    private BigDecimal rate;
    
    // Audit fields (read-only)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
