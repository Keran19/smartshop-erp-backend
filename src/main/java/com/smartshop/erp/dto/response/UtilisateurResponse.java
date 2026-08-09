package com.smartshop.erp.dto.response;

import com.smartshop.erp.enums.RoleUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Vue securisee d'un utilisateur : ne contient jamais le mot de passe (meme hache). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurResponse {
    private Long idUtilisateur;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private RoleUtilisateur role;
    private Boolean actif;
    private Boolean verrouille;
    private Boolean doitChangerMotDePasse;
    private LocalDateTime derniereConnexion;
    private LocalDateTime dateCreation;
}
