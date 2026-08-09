package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Historique de vente d'un produit precis (identifie par son code-barres) sur une periode donnee. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueVenteProduitResponse {
    private String codeBarres;
    private String nomProduit;
    private LocalDateTime periodeDebut;
    private LocalDateTime periodeFin;
    private Integer quantiteTotale;
    private BigDecimal montantTotal;
    private BigDecimal beneficeTotal;
    private List<VenteLigneHistoriqueResponse> ventes;
}
