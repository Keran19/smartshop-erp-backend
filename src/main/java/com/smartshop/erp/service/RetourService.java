package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.RetourRequest;
import com.smartshop.erp.dto.response.RetourResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface RetourService {

    /**
     * Enregistre un retour client (remboursement, echange meme valeur, ou echange valeur
     * differente). Met a jour le stock : les produits rendus reintegrent le stock, les
     * produits donnes en echange en sortent (apres verification de disponibilite).
     */
    RetourResponse creer(RetourRequest request, Long idUtilisateurConnecte);

    RetourResponse obtenir(Long idRetour);

    List<RetourResponse> parVente(Long idVente);

    List<RetourResponse> historique(LocalDateTime debut, LocalDateTime fin, Long idBoutique);

    void annuler(Long idRetour);
}
