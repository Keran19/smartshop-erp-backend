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
public class PaiementCreditResponse {
    private Long idPaiement;
    private BigDecimal montant;
    private LocalDateTime datePaiement;
    private String utilisateur;
    private String observation;
}
