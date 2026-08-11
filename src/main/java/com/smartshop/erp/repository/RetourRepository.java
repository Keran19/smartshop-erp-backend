package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Retour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    // --- Impact caisse, scope par vendeur (utilisateur ayant traite le retour) ---

    /** Argent sorti de la caisse (remboursements purs + difference rendue lors d'un echange a la baisse). */
    @Query("SELECT COALESCE(SUM(r.montantRembourse), 0) FROM Retour r WHERE r.utilisateur.idUtilisateur = :idVendeur " +
           "AND r.statut = com.smartshop.erp.enums.StatutRetour.VALIDE AND r.dateRetour BETWEEN :debut AND :fin")
    BigDecimal sommeRembourseParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                                   @Param("debut") LocalDateTime debut,
                                                   @Param("fin") LocalDateTime fin);

    /** Argent entre en caisse (complement paye par le client lors d'un echange vers un article plus cher). */
    @Query("SELECT COALESCE(SUM(r.montantComplement), 0) FROM Retour r WHERE r.utilisateur.idUtilisateur = :idVendeur " +
           "AND r.statut = com.smartshop.erp.enums.StatutRetour.VALIDE AND r.dateRetour BETWEEN :debut AND :fin")
    BigDecimal sommeComplementParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                                    @Param("debut") LocalDateTime debut,
                                                    @Param("fin") LocalDateTime fin);

    /** Liste detaillee des retours valides du vendeur sur la periode, pour le journal de caisse. */
    @Query("SELECT r FROM Retour r WHERE r.utilisateur.idUtilisateur = :idVendeur " +
           "AND r.statut = com.smartshop.erp.enums.StatutRetour.VALIDE AND r.dateRetour BETWEEN :debut AND :fin " +
           "ORDER BY r.dateRetour DESC")
    List<Retour> listeParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                           @Param("debut") LocalDateTime debut,
                                           @Param("fin") LocalDateTime fin);
}
