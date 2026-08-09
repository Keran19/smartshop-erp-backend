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
public class ProduitVenduResponse {
    private Long idProduit;
    private String nom;
    private String reference;
    private String codeBarres;
    private Long quantiteVendue;
    private BigDecimal montantVentes;
    private BigDecimal beneficeGenere;
}
