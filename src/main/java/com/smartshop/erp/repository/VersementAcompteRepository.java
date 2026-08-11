package com.smartshop.erp.repository;

import com.smartshop.erp.entity.VersementAcompte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface VersementAcompteRepository extends JpaRepository<VersementAcompte, Long> {

    List<VersementAcompte> findByAcompte_IdAcompteOrderByDateVersementDesc(Long idAcompte);

    /** Total des acomptes encaisses par ce vendeur sur la periode (impacte la caisse). */
    @Query("SELECT COALESCE(SUM(v.montant), 0) FROM VersementAcompte v WHERE v.utilisateur.idUtilisateur = :idVendeur " +
           "AND v.dateVersement BETWEEN :debut AND :fin")
    BigDecimal sommeParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                         @Param("debut") LocalDateTime debut,
                                         @Param("fin") LocalDateTime fin);

    /** Liste detaillee des acomptes recus par ce vendeur, pour le journal de caisse. */
    @Query("SELECT v FROM VersementAcompte v WHERE v.utilisateur.idUtilisateur = :idVendeur " +
           "AND v.dateVersement BETWEEN :debut AND :fin ORDER BY v.dateVersement DESC")
    List<VersementAcompte> listeParVendeurEtPeriode(@Param("idVendeur") Long idVendeur,
                                                     @Param("debut") LocalDateTime debut,
                                                     @Param("fin") LocalDateTime fin);
}
