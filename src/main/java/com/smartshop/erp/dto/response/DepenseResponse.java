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
public class DepenseResponse {
    private Long idDepense;
    private Long idBoutique;
    private String boutique;
    private String libelle;
    private String categorie;
    private BigDecimal montant;
    private String observation;
    private LocalDateTime dateDepense;
    private String utilisateur;
}
