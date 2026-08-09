package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "ligne_vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneVente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ligne_vente")
    private Long idLigneVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private Integer quantite;

    @Column(name = "prix_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixUnitaire;

    // Cout unitaire (prix d'achat) fige au moment de la vente -> sert au calcul du benefice
    @Column(name = "prix_achat_unitaire", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal prixAchatUnitaire = BigDecimal.ZERO;

    @Column(name = "sous_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal sousTotal;

    @Transient
    public BigDecimal getBenefice() {
        BigDecimal cout = prixAchatUnitaire == null ? BigDecimal.ZERO : prixAchatUnitaire;
        return prixUnitaire.subtract(cout).multiply(BigDecimal.valueOf(quantite));
    }
}
