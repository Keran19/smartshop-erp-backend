package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.ApprovisionnementRequest;
import com.smartshop.erp.dto.response.ApprovisionnementResponse;

import java.util.List;

public interface ApprovisionnementService {

    List<ApprovisionnementResponse> lister(Long idBoutique);

    ApprovisionnementResponse obtenir(Long id);

    /**
     * Enregistre un approvisionnement recu immediatement : incremente le stock de la boutique
     * destinataire pour chaque ligne (via le trigger SQL trg_entree_stock), journalise
     * l'historique d'achat par produit, et met a jour le prix d'achat courant du produit
     * (utilise pour le calcul du benefice sur les prochaines ventes).
     */
    ApprovisionnementResponse creer(ApprovisionnementRequest request, Long idGerantConnecte);
}
