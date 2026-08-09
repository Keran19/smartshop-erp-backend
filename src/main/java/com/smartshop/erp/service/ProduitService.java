package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.ProduitRequest;
import com.smartshop.erp.dto.response.HistoriqueVenteProduitResponse;
import com.smartshop.erp.dto.response.ProduitResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ProduitService {

    /** Liste de tous les produits avec leur disponibilite en stock (toutes boutiques ou une boutique precise) */
    List<ProduitResponse> listerTous(Long idBoutique);

    ProduitResponse obtenirParId(Long idProduit);

    /**
     * Recherche par code-barres, utilisee notamment lors du scan pour le panier de vente.
     * Leve ProduitIntrouvableParCodeBarresException si le produit n'existe pas, afin que le
     * frontend redirige vers l'ecran d'ajout de produit.
     */
    ProduitResponse obtenirParCodeBarres(String codeBarres);

    List<ProduitResponse> rechercher(String motCle);

    ProduitResponse creer(ProduitRequest request);

    ProduitResponse modifier(Long idProduit, ProduitRequest request);

    void desactiver(Long idProduit);

    List<ProduitResponse> produitsEnAlerte(Long idBoutique);

    /** Historique de vente d'un produit (par code-barres) sur une periode donnee par l'utilisateur. */
    HistoriqueVenteProduitResponse historiqueVentes(String codeBarres, LocalDateTime debut, LocalDateTime fin);
}
