package com.smartshop.erp.repository;

import com.smartshop.erp.entity.SessionCaisse;
import com.smartshop.erp.enums.StatutSessionCaisse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {
    Optional<SessionCaisse> findByBoutique_IdBoutiqueAndStatut(Long idBoutique, StatutSessionCaisse statut);
    Optional<SessionCaisse> findByUtilisateur_IdUtilisateurAndStatut(Long idUtilisateur, StatutSessionCaisse statut);
    List<SessionCaisse> findByBoutique_IdBoutiqueOrderByDateOuvertureDesc(Long idBoutique);
    List<SessionCaisse> findByUtilisateur_IdUtilisateurOrderByDateOuvertureDesc(Long idUtilisateur);

    /** Toutes les sessions ouvertes ce jour-la (bornes horodatees), pour le tableau de bord admin. */
    List<SessionCaisse> findByDateOuvertureBetweenOrderByDateOuvertureDesc(LocalDateTime debutJour, LocalDateTime finJour);
}
