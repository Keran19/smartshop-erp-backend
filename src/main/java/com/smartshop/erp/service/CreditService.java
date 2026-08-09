package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.PaiementCreditRequest;
import com.smartshop.erp.dto.response.CreditResponse;
import com.smartshop.erp.enums.StatutCredit;

import java.util.List;

public interface CreditService {

    List<CreditResponse> lister(StatutCredit statut);

    CreditResponse obtenir(Long idCredit);

    List<CreditResponse> parClient(Long idClient);

    /** Enregistre un paiement partiel ou total ; le solde et le statut sont mis a jour automatiquement. */
    CreditResponse enregistrerPaiement(Long idCredit, PaiementCreditRequest request, Long idUtilisateurConnecte);
}
