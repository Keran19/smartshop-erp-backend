package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historique_approvisionnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoriqueApprovisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historique")
    private Long idHistorique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_approvisionnement", nullable = false)
    private Approvisionnement approvisionnement;

    @Column(nullable = false)
    private Integer quantite;

    /**
     * Quantite de ce lot pas encore ecoulee. Decrementee au fil des ventes (methode FIFO :
     * on puise toujours dans le lot le plus ancien qui a encore du stock). Permet de savoir
     * en permanence a quel prix d'achat correspond la marchandise en train d'etre vendue.
     */
    @Column(name = "quantite_restante", nullable = false)
    @Builder.Default
    private Integer quantiteRestante = 0;

    @Column(name = "prix_achat", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "date_entree")
    private LocalDateTime dateEntree;

    @PrePersist
    public void prePersist() {
        if (dateEntree == null) dateEntree = LocalDateTime.now();
    }
}
