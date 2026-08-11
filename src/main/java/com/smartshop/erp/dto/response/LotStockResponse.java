package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotStockResponse {
    private Long idHistorique;
    private String numeroApprovisionnement;
    private String fournisseur;
    private BigDecimal prixAchat;
    private Integer quantiteInitiale;
    private Integer quantiteRestante;
    private LocalDateTime dateEntree;

    /** true si c'est le lot le plus ancien encore actif : celui qui est en train d'etre ecoule. */
    private boolean lotActuel;
}
