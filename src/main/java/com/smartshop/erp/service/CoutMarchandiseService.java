package com.smartshop.erp.service;

import com.smartshop.erp.dto.response.LotActuelResponse;
import com.smartshop.erp.dto.response.LotStockResponse;

import java.util.List;

public interface CoutMarchandiseService {

    /**
     * Decompte "quantite" unites du produit dans les lots d'approvisionnement de cette boutique,
     * du plus ancien au plus recent (FIFO), a appeler a chaque vente validee. Si les lots connus
     * ne couvrent pas toute la quantite (stock entre autrement que par un approvisionnement
     * enregistre), la depreciation s'arrete simplement au dernier lot disponible sans erreur.
     */
    void deprecierFifo(Long idProduit, Long idBoutique, int quantite);

    /** Le lot le plus ancien encore actif pour ce produit dans cette boutique, avec alerte de marge. */
    LotActuelResponse lotActuel(Long idProduit, Long idBoutique);

    /** Tous les lots (epuises ou non) d'un produit dans une boutique, du plus recent au plus ancien. */
    List<LotStockResponse> lots(Long idProduit, Long idBoutique);
}
