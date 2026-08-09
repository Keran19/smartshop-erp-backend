package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneVenteResponse {
    private Long idProduit;
    private String nomProduit;
    private String codeBarres;
    private Integer quantite;
    private BigDecimal prixUnitaire;
    private BigDecimal sousTotal;
    /** Benefice de la ligne = (prix vente - prix achat) x quantite */
    private BigDecimal benefice;
}
