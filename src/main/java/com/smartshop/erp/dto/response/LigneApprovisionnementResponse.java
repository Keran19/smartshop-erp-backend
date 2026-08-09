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
public class LigneApprovisionnementResponse {
    private Long idProduit;
    private String nomProduit;
    private Integer quantite;
    private BigDecimal prixAchat;
    private BigDecimal sousTotal;
}
