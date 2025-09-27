package com.bill_api_generator.bill_api_generator.model;

import lombok.*;

@Data @AllArgsConstructor
public class InvoiceTotals {
    private double subtotal;
    private double retencionIrpf;
    private double iva;
    private double total;
}