package com.smartshop.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OuvertureCaisseRequest {
    @NotNull(message = "La boutique est obligatoire")
    private Long idBoutique;

    @NotNull(message = "Le detail des coupures est obligatoire")
    @Valid
    private DetailCoupureRequest coupures;

    private String observation;
}
