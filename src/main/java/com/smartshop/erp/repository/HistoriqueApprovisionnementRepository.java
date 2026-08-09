package com.smartshop.erp.repository;

import com.smartshop.erp.entity.HistoriqueApprovisionnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueApprovisionnementRepository extends JpaRepository<HistoriqueApprovisionnement, Long> {
    List<HistoriqueApprovisionnement> findByProduit_IdProduitOrderByDateEntreeDesc(Long idProduit);
}
