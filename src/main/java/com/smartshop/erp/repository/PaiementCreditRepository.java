package com.smartshop.erp.repository;

import com.smartshop.erp.entity.PaiementCredit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaiementCreditRepository extends JpaRepository<PaiementCredit, Long> {
    List<PaiementCredit> findByCredit_IdCreditOrderByDatePaiementDesc(Long idCredit);
}
