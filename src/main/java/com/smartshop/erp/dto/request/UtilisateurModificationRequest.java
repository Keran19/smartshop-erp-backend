package com.smartshop.erp.dto.request;

import com.smartshop.erp.enums.RoleUtilisateur;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Modification d'un compte existant (hors mot de passe, gere par un endpoint dedie). */
@Data
public class UtilisateurModificationRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    @NotNull(message = "Le role est obligatoire")
    private RoleUtilisateur role;

    private Boolean actif;
}
