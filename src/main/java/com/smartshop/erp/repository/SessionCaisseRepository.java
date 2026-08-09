package com.smartshop.erp.repository;

import com.smartshop.erp.entity.SessionCaisse;
import com.smartshop.erp.enums.StatutSessionCaisse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SessionCaisseRepository extends JpaRepository<SessionCaisse, Long> {
    Optional<SessionCaisse> findByBoutique_IdBoutiqueAndStatut(Long idBoutique, StatutSessionCaisse statut);
    List<SessionCaisse> findByBoutique_IdBoutiqueOrderByDateOuvertureDesc(Long idBoutique);
    List<SessionCaisse> findByUtilisateur_IdUtilisateurOrderByDateOuvertureDesc(Long idUtilisateur);
}
