package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_produit")
    private Long idProduit;

    @Column(name = "code_barres", nullable = false, unique = true, length = 100)
    private String codeBarres;

    @Column(unique = true, length = 100)
    private String reference;

    @Column(nullable = false, length = 200)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Prix d'achat courant (cout) - sert au calcul du benefice
    @Column(name = "prix_achat", precision = 12, scale = 2)
    private BigDecimal prixAchat;

    // Prix de vente catalogue (prix unitaire de vente par defaut)
    @Column(name = "prix_catalogue", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixCatalogue;

    @Column(name = "seuil_alerte")
    @Builder.Default
    private Integer seuilAlerte = 0;

    @Column(length = 255)
    private String image;

    @Column(name = "poids_g", precision = 10, scale = 2)
    private BigDecimal poidsG;

    @Column(name = "volume_ml", precision = 10, scale = 2)
    private BigDecimal volumeMl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur")
    private Fournisseur fournisseur;

    @Builder.Default
    private Boolean actif = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categorie", nullable = false)
    private Categorie categorie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marque")
    private Marque marque;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (actif == null) actif = true;
        if (seuilAlerte == null) seuilAlerte = 0;
    }
}
