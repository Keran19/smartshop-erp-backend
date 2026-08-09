package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** Un produit rendu par le client dans le cadre d'un retour. */
@Entity
@Table(name = "ligne_retour")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneRetour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_retour")
    private Long idLigneRetour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_retour", nullable = false)
    private Retour retour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    /** Prix unitaire auquel le produit avait ete vendu (repris de la ligne_vente d'origine). */
    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;

    @Column(length = 255)
    private String motif;
}
