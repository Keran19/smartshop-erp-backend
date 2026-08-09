package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Approvisionnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovisionnementRepository extends JpaRepository<Approvisionnement, Long> {
    Optional<Approvisionnement> findByNumeroApprovisionnement(String numero);
}
