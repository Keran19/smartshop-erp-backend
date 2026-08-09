package com.smartshop.erp.repository;

import com.smartshop.erp.entity.MouvementStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MouvementStockRepository extends JpaRepository<MouvementStock, Long> {

    @Query("SELECT m FROM MouvementStock m WHERE m.produit.idProduit = :idProduit " +
           "AND m.dateMouvement BETWEEN :debut AND :fin ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByProduitAndPeriode(@Param("idProduit") Long idProduit,
                                                  @Param("debut") LocalDateTime debut,
                                                  @Param("fin") LocalDateTime fin);

    @Query("SELECT m FROM MouvementStock m WHERE m.produit.codeBarres = :codeBarres " +
           "AND m.dateMouvement BETWEEN :debut AND :fin ORDER BY m.dateMouvement DESC")
    List<MouvementStock> findByCodeBarresAndPeriode(@Param("codeBarres") String codeBarres,
                                                     @Param("debut") LocalDateTime debut,
                                                     @Param("fin") LocalDateTime fin);
}
