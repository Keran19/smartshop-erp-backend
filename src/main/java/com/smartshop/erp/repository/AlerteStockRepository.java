package com.smartshop.erp.repository;

import com.smartshop.erp.entity.AlerteStock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlerteStockRepository extends JpaRepository<AlerteStock, Long> {
    List<AlerteStock> findByStatut(String statut);
}
