package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.VenteRequest;
import com.smartshop.erp.dto.response.VenteResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface VenteService {

    /**
     * Calcule un apercu de la vente (totaux, monnaie a rendre) SANS rien enregistrer en base.
     * Utilise pour l'ecran "confirmer l'impression" avant validation finale de la vente.
     */
    VenteResponse apercu(VenteRequest request, Long idVendeurConnecte);

    /** Enregistre definitivement la vente : deduit le stock, cree la facture, le credit eventuel. */
    VenteResponse valider(VenteRequest request, Long idVendeurConnecte);

    VenteResponse obtenir(Long idVente);

    /** Historique des ventes sur une periode choisie par l'utilisateur (+ filtre boutique optionnel). */
    List<VenteResponse> historique(LocalDateTime debut, LocalDateTime fin, Long idBoutique);

    void annuler(Long idVente);

    /** Genere le PDF de la facture/recu et marque la facture comme imprimee. Retourne le chemin du fichier genere. */
    String imprimer(Long idVente);

    /** Genere un PDF listant toutes les ventes d'une periode (telechargeable). Retourne le chemin du fichier. */
    String genererRapportPdf(LocalDateTime debut, LocalDateTime fin, Long idBoutique);
}
