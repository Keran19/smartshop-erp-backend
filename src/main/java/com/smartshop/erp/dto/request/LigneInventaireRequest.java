package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class LigneInventaireRequest {
    @NotNull(message = "Le produit est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite physique comptee est obligatoire")
    @PositiveOrZero
    private Integer quantitePhysique;
}
