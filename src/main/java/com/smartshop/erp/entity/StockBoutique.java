package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_boutique", uniqueConstraints = @UniqueConstraint(columnNames = {"id_produit", "id_boutique"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockBoutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_stock")
    private Long idStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @Column(name = "quantite_disponible")
    @Builder.Default
    private Integer quantiteDisponible = 0;

    @Column(name = "prix_vente", precision = 12, scale = 2)
    private BigDecimal prixVente;

    @Column(name = "seuil_alerte")
    private Integer seuilAlerte;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_mise_a_jour")
    private LocalDateTime dateMiseAJour;

    @PrePersist
    @PreUpdate
    public void prePersist() {
        dateMiseAJour = LocalDateTime.now();
        if (quantiteDisponible == null) quantiteDisponible = 0;
        if (actif == null) actif = true;
    }
}
