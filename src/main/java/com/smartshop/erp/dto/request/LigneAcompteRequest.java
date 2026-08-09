package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneAcompteRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite est obligatoire")
    @Positive
    private Integer quantite;

    /** Si non fourni, le prix courant de la boutique (ou catalogue) est utilise. */
    private BigDecimal prixUnitaire;
}
