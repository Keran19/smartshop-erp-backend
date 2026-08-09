package com.smartshop.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class InventaireRequest {
    @NotNull(message = "La boutique est obligatoire")
    private Long idBoutique;

    @NotEmpty(message = "Au moins un produit compte est requis")
    @Valid
    private List<LigneInventaireRequest> lignes;

    private String observation;
}
