package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBoutiqueResponse {
    private Long idBoutique;
    private String nomBoutique;
    private Integer quantiteDisponible;
    private BigDecimal prixVente;
    private Integer seuilAlerte;
    private Boolean enAlerte;
}
