package com.smartshop.erp.dto.request;

import com.smartshop.erp.validation.MotDePasseFort;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Reinitialisation du mot de passe d'un utilisateur par un ADMIN (sans connaitre l'ancien). */
@Data
public class ReinitialiserMotDePasseRequest {
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @MotDePasseFort
    private String nouveauMotDePasse;
}
