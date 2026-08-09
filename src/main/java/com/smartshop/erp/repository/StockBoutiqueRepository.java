package com.smartshop.erp.repository;

import com.smartshop.erp.entity.StockBoutique;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockBoutiqueRepository extends JpaRepository<StockBoutique, Long> {
    Optional<StockBoutique> findByProduit_IdProduitAndBoutique_IdBoutique(Long idProduit, Long idBoutique);
    List<StockBoutique> findByBoutique_IdBoutique(Long idBoutique);
    List<StockBoutique> findByProduit_IdProduit(Long idProduit);
}
