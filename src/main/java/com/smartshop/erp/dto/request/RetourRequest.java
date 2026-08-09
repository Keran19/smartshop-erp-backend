package com.smartshop.erp.dto.request;

import com.smartshop.erp.enums.TypeRetour;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RetourRequest {

    @NotNull(message = "La vente d'origine est obligatoire")
    private Long idVente;

    @NotNull(message = "Le type de retour est obligatoire")
    private TypeRetour typeRetour;

    @NotEmpty(message = "Au moins un produit retourne est requis")
    @Valid
    private List<LigneRetourRequest> lignesRetour;

    /** Obligatoire uniquement pour les types ECHANGE_MEME_VALEUR / ECHANGE_VALEUR_DIFFERENTE. */
    @Valid
    private List<LigneEchangeRequest> lignesEchange;

    private String observation;
}
