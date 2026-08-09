package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneInventaireResponse {
    private Long idProduit;
    private String nomProduit;
    private Integer quantiteTheorique;
    private Integer quantitePhysique;
    private Integer ecart;
}
