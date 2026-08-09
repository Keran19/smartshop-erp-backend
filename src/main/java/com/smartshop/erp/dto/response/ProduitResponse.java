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
public class ProduitResponse {
    private Long idProduit;
    private String codeBarres;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prixAchat;
    private BigDecimal prixVente;
    private Integer seuilAlerte;
    private String image;
    private BigDecimal poidsG;
    private BigDecimal volumeMl;
    private String categorie;
    private Long idCategorie;
    private String marque;
    private Long idMarque;
    private String fournisseur;
    private Long idFournisseur;
    private Boolean actif;
    private LocalDateTime dateCreation;
    /** Disponibilite en stock, boutique par boutique */
    private List<StockBoutiqueResponse> stocks;
    /** Quantite disponible totale, toutes boutiques confondues */
    private Integer quantiteTotale;
}
