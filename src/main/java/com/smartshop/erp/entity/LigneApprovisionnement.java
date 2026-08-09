package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_approvisionnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneApprovisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_approvisionnement")
    private Long idLigneApprovisionnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_approvisionnement", nullable = false)
    private Approvisionnement approvisionnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_achat", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;
}
