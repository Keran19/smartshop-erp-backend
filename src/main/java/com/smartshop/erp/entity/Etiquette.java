package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "etiquette")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Etiquette {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_etiquette")
    private Long idEtiquette;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Column(name = "quantite_imprimee", nullable = false)
    private Integer quantiteImprimee;

    @Column(name = "date_generation")
    private LocalDateTime dateGeneration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (dateGeneration == null) dateGeneration = LocalDateTime.now();
    }
}
