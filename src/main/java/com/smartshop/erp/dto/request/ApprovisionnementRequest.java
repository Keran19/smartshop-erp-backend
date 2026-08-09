package com.smartshop.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ApprovisionnementRequest {
    @NotNull(message = "Le fournisseur est obligatoire")
    private Long idFournisseur;

    @NotNull(message = "La boutique destinataire est obligatoire")
    private Long idBoutique;

    @NotEmpty(message = "Au moins un produit est requis")
    @Valid
    private List<LigneApprovisionnementRequest> lignes;

    private String observation;
}
