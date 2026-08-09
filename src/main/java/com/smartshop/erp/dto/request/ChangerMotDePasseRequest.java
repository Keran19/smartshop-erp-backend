package com.smartshop.erp.dto.request;

import com.smartshop.erp.validation.MotDePasseFort;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Changement de son propre mot de passe : necessite de fournir l'ancien. */
@Data
public class ChangerMotDePasseRequest {
    @NotBlank(message = "L'ancien mot de passe est obligatoire")
    private String ancienMotDePasse;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @MotDePasseFort
    private String nouveauMotDePasse;
}
