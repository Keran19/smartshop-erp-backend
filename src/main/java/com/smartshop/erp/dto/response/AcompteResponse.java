package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutAcompte;
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
public class AcompteResponse {
    private Long idAcompte;
    private String numeroAcompte;
    private Long idClient;
    private String client;
    private String telephoneClient;
    private Long idBoutique;
    private String boutique;
    private String vendeur;
    private BigDecimal montantTotal;
    private BigDecimal montantVerse;
    private BigDecimal resteAPayer;
    private StatutAcompte statut;
    private LocalDateTime dateCreation;
    private String observation;
    private List<LigneAcompteResponse> lignes;
    private List<VersementAcompteResponse> versements;
}
