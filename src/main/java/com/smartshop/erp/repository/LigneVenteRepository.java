package com.smartshop.erp.repository;

import com.smartshop.erp.entity.LigneVente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LigneVenteRepository extends JpaRepository<LigneVente, Long> {

    /** Classement des produits les plus vendus sur une periode (quantite totale desc) */
    @Query("SELECT lv.produit.idProduit, lv.produit.nom, lv.produit.reference, lv.produit.codeBarres, " +
           "SUM(lv.quantite), SUM(lv.sousTotal), SUM((lv.prixUnitaire - lv.prixAchatUnitaire) * lv.quantite) " +
           "FROM LigneVente lv " +
           "WHERE lv.vente.statut = com.smartshop.erp.enums.StatutVente.VALIDEE " +
           "AND lv.vente.dateVente BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR lv.vente.boutique.idBoutique = :idBoutique) " +
           "GROUP BY lv.produit.idProduit, lv.produit.nom, lv.produit.reference, lv.produit.codeBarres " +
           "ORDER BY SUM(lv.quantite) DESC")
    List<Object[]> produitsLesPlusVendus(@Param("debut") LocalDateTime debut,
                                          @Param("fin") LocalDateTime fin,
                                          @Param("idBoutique") Long idBoutique);

    /** Historique de vente d'un produit precis (par code-barres) sur une periode */
    @Query("SELECT lv FROM LigneVente lv " +
           "WHERE lv.produit.codeBarres = :codeBarres " +
           "AND lv.vente.dateVente BETWEEN :debut AND :fin " +
           "ORDER BY lv.vente.dateVente DESC")
    List<LigneVente> historiqueParCodeBarres(@Param("codeBarres") String codeBarres,
                                              @Param("debut") LocalDateTime debut,
                                              @Param("fin") LocalDateTime fin);

    List<LigneVente> findByVente_IdVente(Long idVente);

    java.util.Optional<LigneVente> findByVente_IdVenteAndProduit_IdProduit(Long idVente, Long idProduit);

    @Query("SELECT COALESCE(SUM((lv.prixUnitaire - lv.prixAchatUnitaire) * lv.quantite), 0) FROM LigneVente lv " +
           "WHERE lv.vente.statut = com.smartshop.erp.enums.StatutVente.VALIDEE " +
           "AND lv.vente.dateVente BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR lv.vente.boutique.idBoutique = :idBoutique)")
    java.math.BigDecimal beneficeTotalPeriode(@Param("debut") LocalDateTime debut,
                                               @Param("fin") LocalDateTime fin,
                                               @Param("idBoutique") Long idBoutique);
}
