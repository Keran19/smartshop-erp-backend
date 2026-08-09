package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAjustementRequest {
    @NotNull
    private Long idProduit;
    @NotNull
    private Long idBoutique;
    @NotNull
    private Integer quantite;
    private String motif;
}
