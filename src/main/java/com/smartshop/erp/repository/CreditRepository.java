package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Credit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CreditRepository extends JpaRepository<Credit, Long> {
    Optional<Credit> findByVente_IdVente(Long idVente);
    List<Credit> findByClient_IdClient(Long idClient);
    List<Credit> findByStatut(com.smartshop.erp.enums.StatutCredit statut);
}
