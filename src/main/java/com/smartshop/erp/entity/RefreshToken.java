package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Jeton de rafraichissement JWT. On ne stocke JAMAIS le token en clair : seul son
 * hash (SHA-256) est persiste, comme pour un mot de passe. Ceci permet de revoquer
 * un jeton (deconnexion, compromission) sans avoir a attendre son expiration naturelle,
 * et de detecter une reutilisation frauduleuse (rotation a usage unique).
 */
@Entity
@Table(name = "refresh_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_refresh_token")
    private Long idRefreshToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    private String tokenHash;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_expiration", nullable = false)
    private LocalDateTime dateExpiration;

    @Builder.Default
    @Column(name = "revoque")
    private Boolean revoque = false;

    @Column(name = "adresse_ip", length = 50)
    private String adresseIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (revoque == null) revoque = false;
    }

    @Transient
    public boolean estValide() {
        return !Boolean.TRUE.equals(revoque) && dateExpiration.isAfter(LocalDateTime.now());
    }
}
