package com.smartshop.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AcompteRequest {
    @NotNull(message = "Le client est obligatoire")
    private Long idClient;

    @NotNull(message = "La boutique est obligatoire")
    private Long idBoutique;

    @NotEmpty(message = "Au moins un produit est requis")
    @Valid
    private List<LigneAcompteRequest> lignes;

    /** Versement initial optionnel effectue au moment de la creation de l'acompte. */
    private BigDecimal versementInitial;

    private String observation;
}
