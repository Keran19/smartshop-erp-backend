package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Un nouveau produit remis au client en echange, dans le cadre d'un retour avec echange. */
@Entity
@Table(name = "ligne_echange")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneEchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_echange")
    private Long idLigneEchange;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_retour", nullable = false)
    private Retour retour;

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
