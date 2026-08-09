package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "prix_produit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrixProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prix")
    private Long idPrix;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(name = "ancien_prix", precision = 12, scale = 2)
    private BigDecimal ancienPrix;

    @Column(name = "nouveau_prix", precision = 12, scale = 2)
    private BigDecimal nouveauPrix;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;
}
