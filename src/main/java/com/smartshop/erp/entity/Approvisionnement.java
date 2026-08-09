package com.smartshop.erp.entity;

import com.smartshop.erp.enums.StatutApprovisionnement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "approvisionnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Approvisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_approvisionnement")
    private Long idApprovisionnement;

    @Column(name = "numero_approvisionnement", nullable = false, unique = true, length = 50)
    private String numeroApprovisionnement;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fournisseur", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gerant", nullable = false)
    private Utilisateur gerant;

    @Column(name = "montant_total", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutApprovisionnement statut = StatutApprovisionnement.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @OneToMany(mappedBy = "approvisionnement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneApprovisionnement> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutApprovisionnement.EN_ATTENTE;
    }
}
