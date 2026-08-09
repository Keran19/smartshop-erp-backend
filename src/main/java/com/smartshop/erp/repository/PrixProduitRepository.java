package com.smartshop.erp.repository;

import com.smartshop.erp.entity.PrixProduit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrixProduitRepository extends JpaRepository<PrixProduit, Long> {
    List<PrixProduit> findByProduit_IdProduitOrderByDateModificationDesc(Long idProduit);
}
