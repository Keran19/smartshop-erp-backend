package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiqueResponse {
    private LocalDateTime periodeDebut;
    private LocalDateTime periodeFin;
    private Long idBoutique;

    private BigDecimal chiffreAffaires;
    private Long nombreVentes;
    private BigDecimal beneficeTotal;
    private Long nombreNouveauxClients;

    /** Le produit le plus vendu sur la periode (null si aucune vente). */
    private ProduitVenduResponse produitLePlusVendu;

    /** Classement complet des produits les plus vendus (utile pour un graphique/top 10). */
    private List<ProduitVenduResponse> classementProduits;
}
