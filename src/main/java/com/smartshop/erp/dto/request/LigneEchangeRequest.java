package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LigneEchangeRequest {
    @NotNull(message = "Le produit donne en echange est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantite;
}
