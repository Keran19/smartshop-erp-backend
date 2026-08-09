package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.InventaireRequest;
import com.smartshop.erp.dto.response.InventaireResponse;

import java.util.List;

public interface InventaireService {

    List<InventaireResponse> lister(Long idBoutique);

    InventaireResponse obtenir(Long idInventaire);

    /**
     * Enregistre un inventaire physique : pour chaque produit compte, calcule l'ecart avec le
     * stock theorique, ajuste immediatement le stock de la boutique sur la quantite physique
     * constatee, et journalise un mouvement de type AJUSTEMENT_INVENTAIRE.
     */
    InventaireResponse creer(InventaireRequest request, Long idUtilisateurConnecte);
}
