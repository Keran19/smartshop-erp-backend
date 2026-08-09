package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    /** Duree de validite de l'access token, en secondes (pour que le frontend planifie le refresh). */
    private Long expiresInSecondes;
    private Long idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private RoleUtilisateur role;
    /** Si vrai, le frontend doit imperativement rediriger vers le changement de mot de passe. */
    private Boolean doitChangerMotDePasse;
}
