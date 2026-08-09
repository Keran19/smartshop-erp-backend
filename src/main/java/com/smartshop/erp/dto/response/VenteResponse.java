package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.ModeReglement;
import com.smartshop.erp.enums.StatutVente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VenteResponse {
    private Long idVente;
    private String numeroVente;
    private LocalDateTime dateVente;
    private Long idBoutique;
    private String boutique;
    private Long idClient;
    private String client;
    private Long idVendeur;
    private String vendeur;
    private BigDecimal montantTotal;
    private BigDecimal remiseGlobale;
    private BigDecimal montantFinal;
    private BigDecimal montantRecu;
    /** Monnaie a rendre = montant recu - montant final, calculee automatiquement */
    private BigDecimal monnaieRendue;
    private ModeReglement modeReglement;
    private StatutVente statut;
    private String observation;
    /** Benefice total de la vente */
    private BigDecimal benefice;
    private List<LigneVenteResponse> lignes;
    private String numeroFacture;
    private Boolean facturedImprimee;
}
