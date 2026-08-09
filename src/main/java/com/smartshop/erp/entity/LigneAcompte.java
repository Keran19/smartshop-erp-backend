package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_acompte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneAcompte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_acompte")
    private Long idLigneAcompte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acompte", nullable = false)
    private Acompte acompte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;
}
