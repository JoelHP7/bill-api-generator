package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    
    Optional<Factura> findByNumeroFactura(String numeroFactura);
    
    List<Factura> findByClienteId(Long clienteId);
    
    @Query("SELECT COALESCE(MAX(f.numeroSecuencial), 69) FROM Factura f")
    Long findMaxNumeroSecuencial();
    
    Optional<Factura> findByClienteIdAndFechaAndHoras(Long clienteId, LocalDate fecha, Integer horas);
}
