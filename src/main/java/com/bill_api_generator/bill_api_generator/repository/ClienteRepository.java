package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByCif(String cif);
    
    boolean existsByCif(String cif);
    
    Optional<Cliente> findByRef(String ref);
    
    Optional<Cliente> findByRefAndDeletedAtIsNull(String ref);
    
    boolean existsByRef(String ref);
    
    List<Cliente> findByDeletedAtIsNull();  // Solo clientes activos
}
