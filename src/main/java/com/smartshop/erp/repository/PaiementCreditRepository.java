package com.smartshop.erp.repository;

import com.smartshop.erp.entity.PaiementCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface PaiementCreditRepository extends JpaRepository<PaiementCredit, Long> {

    List<PaiementCredit> findByCredit_IdCreditOrderByDatePaiementDesc(Long idCredit);

    /** Total des remboursements de credit encaisses par ce vendeur sur la periode (impacte la caisse). */
    @Query("SELECT COALESCE(SUM(p.montant), 0) FROM PaiementCredit p WHERE p.utilisateur.idUtilisateur = :idVendeur " +
           "AND p.datePaiement BETWEEN :debut AND :fin")
    BigDecimal sommeParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                         @Param("debut") LocalDateTime debut,
                                         @Param("fin") LocalDateTime fin);

    /** Liste detaillee des remboursements de credit encaisses par ce vendeur, pour le journal de caisse. */
    @Query("SELECT p FROM PaiementCredit p WHERE p.utilisateur.idUtilisateur = :idVendeur " +
           "AND p.datePaiement BETWEEN :debut AND :fin ORDER BY p.datePaiement DESC")
    List<PaiementCredit> listeParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                                   @Param("debut") LocalDateTime debut,
                                                   @Param("fin") LocalDateTime fin);
}
