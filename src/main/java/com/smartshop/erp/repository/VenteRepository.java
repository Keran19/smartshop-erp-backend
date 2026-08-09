package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Vente;
import com.smartshop.erp.enums.StatutVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VenteRepository extends JpaRepository<Vente, Long> {

    Optional<Vente> findByNumeroVente(String numeroVente);

    @Query("SELECT v FROM Vente v WHERE v.dateVente BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR v.boutique.idBoutique = :idBoutique) " +
           "ORDER BY v.dateVente DESC")
    List<Vente> findByPeriode(@Param("debut") LocalDateTime debut,
                               @Param("fin") LocalDateTime fin,
                               @Param("idBoutique") Long idBoutique);

    @Query("SELECT COALESCE(SUM(v.montantFinal), 0) FROM Vente v WHERE v.statut = :statut " +
           "AND v.dateVente BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR v.boutique.idBoutique = :idBoutique)")
    BigDecimal sommeChiffreAffaire(@Param("debut") LocalDateTime debut,
                                    @Param("fin") LocalDateTime fin,
                                    @Param("statut") StatutVente statut,
                                    @Param("idBoutique") Long idBoutique);

    @Query("SELECT COALESCE(SUM(v.montantFinal), 0) FROM Vente v WHERE v.statut = com.smartshop.erp.enums.StatutVente.VALIDEE " +
           "AND v.modeReglement = com.smartshop.erp.enums.ModeReglement.COMPTANT " +
           "AND v.boutique.idBoutique = :idBoutique AND v.dateVente BETWEEN :debut AND :fin")
    BigDecimal sommeVentesComptantParBoutiqueEtPeriode(@Param("idBoutique") Long idBoutique,
                                                         @Param("debut") LocalDateTime debut,
                                                         @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(v) FROM Vente v WHERE v.statut = :statut " +
           "AND v.dateVente BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR v.boutique.idBoutique = :idBoutique)")
    long nombreVentes(@Param("debut") LocalDateTime debut,
                       @Param("fin") LocalDateTime fin,
                       @Param("statut") StatutVente statut,
                       @Param("idBoutique") Long idBoutique);
}
