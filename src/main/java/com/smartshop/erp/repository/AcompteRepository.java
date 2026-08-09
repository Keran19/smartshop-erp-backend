package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Acompte;
import com.smartshop.erp.enums.StatutAcompte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcompteRepository extends JpaRepository<Acompte, Long> {
    Optional<Acompte> findByNumeroAcompte(String numeroAcompte);
    List<Acompte> findByClient_IdClient(Long idClient);
    List<Acompte> findByStatut(StatutAcompte statut);
    List<Acompte> findByBoutique_IdBoutique(Long idBoutique);
}
