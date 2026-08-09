package com.smartshop.erp.entity;

import com.smartshop.erp.enums.StatutAcompte;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "acompte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Acompte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_acompte")
    private Long idAcompte;

    @Column(name = "numero_acompte", nullable = false, unique = true, length = 50)
    private String numeroAcompte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendeur", nullable = false)
    private Utilisateur vendeur;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "montant_verse", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantVerse = BigDecimal.ZERO;

    @Column(name = "reste_a_payer", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal resteAPayer = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutAcompte statut = StatutAcompte.EN_ATTENTE;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @OneToMany(mappedBy = "acompte", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneAcompte> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutAcompte.EN_ATTENTE;
    }
}
