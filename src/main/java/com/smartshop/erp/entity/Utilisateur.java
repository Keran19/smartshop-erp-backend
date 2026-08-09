package com.smartshop.erp.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.smartshop.erp.enums.RoleUtilisateur;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_utilisateur")
    private Long idUtilisateur;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @JsonIgnore
    @Column(name = "mot_de_passe", nullable = false, length = 255)
    private String motDePasse;

    @Column(length = 30)
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleUtilisateur role;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    // ---------------------------------------------------------------
    // Securite : anti-bruteforce, verrouillage de compte, hygiene des mots de passe
    // ---------------------------------------------------------------

    /** Nombre de tentatives de connexion echouees consecutives. Remis a zero a chaque succes. */
    @Column(name = "tentatives_echouees")
    @Builder.Default
    private Integer tentativesEchouees = 0;

    /** Si renseigne et dans le futur, le compte est verrouille jusqu'a cette date/heure. */
    @Column(name = "verrouille_jusqua")
    private LocalDateTime verrouilleJusqua;

    /** Force le changement de mot de passe a la prochaine connexion (ex: compte cree par un admin). */
    @Column(name = "doit_changer_mot_de_passe")
    @Builder.Default
    private Boolean doitChangerMotDePasse = true;

    @Column(name = "derniere_connexion")
    private LocalDateTime derniereConnexion;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (actif == null) actif = true;
        if (tentativesEchouees == null) tentativesEchouees = 0;
        if (doitChangerMotDePasse == null) doitChangerMotDePasse = true;
    }

    @Transient
    public boolean estVerrouille() {
        return verrouilleJusqua != null && verrouilleJusqua.isAfter(LocalDateTime.now());
    }
}
