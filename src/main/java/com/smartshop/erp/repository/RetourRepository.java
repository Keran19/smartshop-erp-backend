package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Retour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RetourRepository extends JpaRepository<Retour, Long> {

    Optional<Retour> findByNumeroRetour(String numeroRetour);

    List<Retour> findByVente_IdVente(Long idVente);

    @Query("SELECT r FROM Retour r WHERE r.dateRetour BETWEEN :debut AND :fin " +
           "AND (:idBoutique IS NULL OR r.boutique.idBoutique = :idBoutique) " +
           "ORDER BY r.dateRetour DESC")
    List<Retour> findByPeriode(@Param("debut") LocalDateTime debut,
                                @Param("fin") LocalDateTime fin,
                                @Param("idBoutique") Long idBoutique);

    /** Quantite deja retournee (retours valides uniquement) pour un produit donne d'une vente donnee. */
    @Query("SELECT COALESCE(SUM(lr.quantite), 0) FROM LigneRetour lr " +
           "WHERE lr.retour.vente.idVente = :idVente AND lr.produit.idProduit = :idProduit " +
           "AND lr.retour.statut = com.smartshop.erp.enums.StatutRetour.VALIDE")
    Integer quantiteDejaRetournee(@Param("idVente") Long idVente, @Param("idProduit") Long idProduit);
}
