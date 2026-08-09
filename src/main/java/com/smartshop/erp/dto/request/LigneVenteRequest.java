package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneVenteRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantite;

    /** Prix unitaire de vente. Si non fourni, le prix courant de la boutique (ou catalogue) est utilise. */
    private BigDecimal prixUnitaire;
}
