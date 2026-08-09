package com.smartshop.erp.repository;

import com.smartshop.erp.entity.LigneEchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneEchangeRepository extends JpaRepository<LigneEchange, Long> {
    List<LigneEchange> findByRetour_IdRetour(Long idRetour);
}
