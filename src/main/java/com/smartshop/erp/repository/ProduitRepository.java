package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Produit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProduitRepository extends JpaRepository<Produit, Long> {
    Optional<Produit> findByCodeBarres(String codeBarres);
    boolean existsByCodeBarres(String codeBarres);
    List<Produit> findByNomContainingIgnoreCaseOrReferenceContainingIgnoreCaseOrCodeBarresContainingIgnoreCase(
            String nom, String reference, String codeBarres);
}
