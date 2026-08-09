package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DepenseRequest {
    @NotNull(message = "La boutique est obligatoire")
    private Long idBoutique;

    @NotBlank(message = "Le libelle est obligatoire")
    private String libelle;

    private String categorie;

    @NotNull(message = "Le montant est obligatoire")
    @Positive(message = "Le montant doit etre positif")
    private BigDecimal montant;

    private String observation;
}
