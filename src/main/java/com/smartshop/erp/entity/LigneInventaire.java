package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ligne_inventaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneInventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne")
    private Long idLigne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_inventaire", nullable = false)
    private Inventaire inventaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(name = "quantite_theorique", nullable = false)
    private Integer quantiteTheorique;

    @Column(name = "quantite_physique", nullable = false)
    private Integer quantitePhysique;

    @Column(nullable = false)
    private Integer ecart;
}
