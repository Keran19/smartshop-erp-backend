package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Facture;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FactureRepository extends JpaRepository<Facture, Long> {
    Optional<Facture> findByVente_IdVente(Long idVente);
    Optional<Facture> findByNumeroFacture(String numeroFacture);
}
