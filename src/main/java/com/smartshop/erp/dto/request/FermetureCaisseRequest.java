package com.smartshop.erp.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FermetureCaisseRequest {
    @NotNull(message = "Le detail des coupures est obligatoire")
    @Valid
    private DetailCoupureRequest coupures;

    private String observation;
}
