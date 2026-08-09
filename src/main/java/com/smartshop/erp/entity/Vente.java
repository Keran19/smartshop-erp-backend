package com.smartshop.erp.entity;

import com.smartshop.erp.enums.ModeReglement;
import com.smartshop.erp.enums.StatutVente;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vente")
    private Long idVente;

    @Column(name = "numero_vente", nullable = false, unique = true, length = 50)
    private String numeroVente;

    @Column(name = "date_vente")
    private LocalDateTime dateVente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendeur", nullable = false)
    private Utilisateur vendeur;

    @Column(name = "montant_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "remise_globale", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal remiseGlobale = BigDecimal.ZERO;

    @Column(name = "montant_final", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantFinal = BigDecimal.ZERO;

    @Column(name = "montant_recu", precision = 12, scale = 2)
    private BigDecimal montantRecu;

    @Column(name = "monnaie_rendue", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal monnaieRendue = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_reglement", nullable = false, length = 20)
    private ModeReglement modeReglement;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutVente statut = StatutVente.VALIDEE;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @OneToMany(mappedBy = "vente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneVente> lignes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateVente == null) dateVente = LocalDateTime.now();
        if (statut == null) statut = StatutVente.VALIDEE;
        if (montantTotal == null) montantTotal = BigDecimal.ZERO;
        if (remiseGlobale == null) remiseGlobale = BigDecimal.ZERO;
        if (montantFinal == null) montantFinal = BigDecimal.ZERO;
        if (monnaieRendue == null) monnaieRendue = BigDecimal.ZERO;
    }

    /** Benefice total de la vente = somme des benefices de chaque ligne */
    @Transient
    public BigDecimal getBenefice() {
        return lignes.stream()
                .map(LigneVente::getBenefice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
