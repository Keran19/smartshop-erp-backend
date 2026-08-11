package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.StatutSessionCaisse;
import com.smartshop.erp.enums.StatutValidationEcart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Une ligne du tableau de bord admin "Gestion de caisse" : une session, avec tous ses chiffres. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionCaisseAdminResponse {
    private Long idSession;
    private String boutique;
    private String vendeur;
    private LocalDateTime dateOuverture;
    private LocalDateTime dateFermeture;
    private StatutSessionCaisse statut;

    private BigDecimal fondCaisse;
    private BigDecimal montantVenteEspeces;
    private BigDecimal montantVenteCredit;
    private BigDecimal depensesJournee;
    private BigDecimal montantTheoriqueAttendu;
    private BigDecimal montantRenseigne;
    private BigDecimal ecart;

    private StatutValidationEcart statutValidationEcart;
    private String commentaireValidation;
    private BigDecimal montantImputeSalaire;
    private String validateur;
    private LocalDateTime dateValidation;
}
