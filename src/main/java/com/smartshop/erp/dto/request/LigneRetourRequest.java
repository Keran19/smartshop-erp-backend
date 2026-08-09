package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class LigneRetourRequest {
    @NotNull(message = "Le produit retourne est obligatoire")
    private Long idProduit;

    @NotNull(message = "La quantite retournee est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantite;

    private String motif;
}
