package com.smartshop.erp.dto.request;

import com.smartshop.erp.enums.RoleUtilisateur;
import com.smartshop.erp.validation.MotDePasseFort;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/** Creation d'un compte utilisateur (reserve aux ADMIN). */
@Data
public class UtilisateurCreationRequest {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prenom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @MotDePasseFort
    private String motDePasse;

    private String telephone;

    @NotNull(message = "Le role est obligatoire")
    private RoleUtilisateur role;
}
