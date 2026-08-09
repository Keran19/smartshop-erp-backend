package com.smartshop.erp.repository;

import com.smartshop.erp.entity.LigneRetour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LigneRetourRepository extends JpaRepository<LigneRetour, Long> {
    List<LigneRetour> findByRetour_IdRetour(Long idRetour);
}
