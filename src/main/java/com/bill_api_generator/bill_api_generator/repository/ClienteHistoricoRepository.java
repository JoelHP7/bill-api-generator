package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.ClienteHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteHistoricoRepository extends JpaRepository<ClienteHistorico, Long> {
    
    List<ClienteHistorico> findByClienteIdOrderByFechaCambioDesc(Long clienteId);
    
    List<ClienteHistorico> findByOperacion(String operacion);
}
