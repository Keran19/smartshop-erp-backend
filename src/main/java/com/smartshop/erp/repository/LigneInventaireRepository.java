package com.smartshop.erp.repository;

import com.smartshop.erp.entity.LigneInventaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneInventaireRepository extends JpaRepository<LigneInventaire, Long> {
    List<LigneInventaire> findByInventaire_IdInventaire(Long idInventaire);
}
