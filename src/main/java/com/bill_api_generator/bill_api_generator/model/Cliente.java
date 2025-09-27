package com.bill_api_generator.bill_api_generator.model;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Cliente {
    private String nombre;         // p.ej. "AXPE CONSULTING, S.L."
    private String cif;            // p.ej. "B84184548"
    private String direccion;      // p.ej. "Calle Arturo Soria 122, Madrid"
    private String cp;             // p.ej. "08204"
}