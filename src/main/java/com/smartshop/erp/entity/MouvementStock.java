package com.smartshop.erp.entity;

import com.smartshop.erp.enums.TypeMouvement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mouvement_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mouvement")
    private Long idMouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false, length = 30)
    private TypeMouvement typeMouvement;

    @Column(nullable = false)
    private Integer quantite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique_source")
    private Boutique boutiqueSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique_destination")
    private Boutique boutiqueDestination;

    @Column(length = 255)
    private String motif;

    @Column(name = "date_mouvement")
    private LocalDateTime dateMouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (dateMouvement == null) dateMouvement = LocalDateTime.now();
    }
}
