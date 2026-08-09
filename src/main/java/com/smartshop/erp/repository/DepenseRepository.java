package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Depense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface DepenseRepository extends JpaRepository<Depense, Long> {

    @Query("SELECT d FROM Depense d WHERE d.dateDepense BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR d.boutique.idBoutique = :idBoutique) ORDER BY d.dateDepense DESC")
    List<Depense> findByPeriode(@Param("debut") LocalDateTime debut,
                                 @Param("fin") LocalDateTime fin,
                                 @Param("idBoutique") Long idBoutique);

    @Query("SELECT COALESCE(SUM(d.montant),0) FROM Depense d WHERE d.dateDepense BETWEEN :debut AND :fin " +
           "AND d.boutique.idBoutique = :idBoutique")
    BigDecimal sommeParBoutiqueEtPeriode(@Param("idBoutique") Long idBoutique,
                                          @Param("debut") LocalDateTime debut,
                                          @Param("fin") LocalDateTime fin);
}
