package com.bill_api_generator.bill_api_generator.repository;

import com.bill_api_generator.bill_api_generator.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByCif(String cif);
    
    boolean existsByCif(String cif);
}
