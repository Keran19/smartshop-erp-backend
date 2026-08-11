package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotActuelResponse {
    private Long idProduit;
    private String nomProduit;
    private Long idHistorique;
    private BigDecimal prixAchatLotActuel;
    private Integer quantiteRestante;
    private LocalDateTime dateEntree;
    private String fournisseur;

    private BigDecimal prixVenteActuel;
    /** prixVenteActuel - prixAchatLotActuel (peut etre negatif si le prix de vente n'a pas ete ajuste). */
    private BigDecimal margeUnitaire;
    /** true si la marge unitaire est nulle ou negative : vendre au prix actuel fait perdre de l'argent. */
    private boolean alerteMarge;
}
