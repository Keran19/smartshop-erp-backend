package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LigneApprovisionnementRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite est obligatoire")
    @Positive
    private Integer quantite;

    @NotNull(message = "Le prix d'achat est obligatoire")
    @Positive(message = "Le prix d'achat doit etre positif")
    private BigDecimal prixAchat;
}
