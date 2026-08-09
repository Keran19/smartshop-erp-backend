package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutSessionCaisse;
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
public class SessionCaisseResponse {
    private Long idSession;
    private Long idBoutique;
    private String boutique;
    private String utilisateur;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private BigDecimal fondCaisse;
    private BigDecimal montantTheorique;
    private BigDecimal montantCompte;
    private BigDecimal ecart;
    private StatutSessionCaisse statut;
    private String observation;
    private DetailCoupureResponse coupureOuverture;
    private DetailCoupureResponse coupureFermeture;
}
