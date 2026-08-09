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

    @Column(name = "prix_achat", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "date_entree")
    private LocalDateTime dateEntree;

    @PrePersist
    public void prePersist() {
        if (dateEntree == null) dateEntree = LocalDateTime.now();
    }
}
