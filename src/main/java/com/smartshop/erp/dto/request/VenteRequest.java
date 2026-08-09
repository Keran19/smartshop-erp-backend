package com.smartshop.erp.dto.request;

import com.smartshop.erp.enums.ModeReglement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class VenteRequest {

    @NotNull(message = "La boutique est obligatoire")
    private Long idBoutique;

    /** Optionnel : vente au comptant sans client identifie */
    private Long idClient;

    @NotNull(message = "Le mode de reglement est obligatoire")
    private ModeReglement modeReglement;

    @PositiveOrZero
    private BigDecimal remiseGlobale;

    /** Montant recu du client (especes). Sert a calculer automatiquement la monnaie a rendre. */
    private BigDecimal montantRecu;

    /** Date limite de paiement si modeReglement = CREDIT */
    private LocalDate dateLimiteCredit;

    private String observation;

    @NotEmpty(message = "Le panier ne peut pas etre vide")
    @Valid
    private List<LigneVenteRequest> lignes;
}
