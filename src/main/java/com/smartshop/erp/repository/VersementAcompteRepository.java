package com.smartshop.erp.repository;

import com.smartshop.erp.entity.VersementAcompte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VersementAcompteRepository extends JpaRepository<VersementAcompte, Long> {
    List<VersementAcompte> findByAcompte_IdAcompteOrderByDateVersementDesc(Long idAcompte);
}
