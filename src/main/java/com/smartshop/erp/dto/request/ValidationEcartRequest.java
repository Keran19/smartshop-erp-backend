package com.smartshop.erp.dto.request;

import com.smartshop.erp.enums.StatutValidationEcart;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ValidationEcartRequest {

    @NotNull(message = "Le statut de validation est obligatoire (VALIDE ou IMPUTE_SALAIRE)")
    private StatutValidationEcart statut;

    private String commentaire;

    /** Requis uniquement si statut = IMPUTE_SALAIRE. Si absent, la valeur absolue de l'ecart est utilisee. */
    private BigDecimal montantImpute;
}
