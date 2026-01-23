package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Contacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    
    List<Contacto> findByClienteIdAndDeletedAtIsNull(Long clienteId);
    
    Optional<Contacto> findByClienteIdAndPrincipalTrueAndDeletedAtIsNull(Long clienteId);
    
    List<Contacto> findByClienteId(Long clienteId);
}
