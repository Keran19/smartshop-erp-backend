package com.smartshop.erp.entity;

import com.smartshop.erp.enums.StatutSessionCaisse;
import com.smartshop.erp.enums.StatutValidationEcart;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "session_caisse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionCaisse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_session")
    private Long idSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(name = "date_ouverture", nullable = false)
    private LocalDateTime dateOuverture;

    @Column(name = "date_fermeture")
    private LocalDateTime dateFermeture;

    // Montant declare a l'ouverture (calcule a partir des coupures)
    @Column(name = "fond_caisse", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal fondCaisse = BigDecimal.ZERO;

    // Montant theorique en fin de journee = fond_caisse + ventes especes - depenses (+ credits/acomptes/retours)
    @Column(name = "montant_theorique", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantTheorique = BigDecimal.ZERO;

    // Montant reellement compte a la fermeture (calcule a partir des coupures de fermeture)
    @Column(name = "montant_compte", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantCompte = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal ecart = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutSessionCaisse statut = StatutSessionCaisse.OUVERTE;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DetailCoupureSession> coupures = new ArrayList<>();

    // --- Validation de l'ecart par un admin/gerant (page "Gestion de caisse") ---

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_validation_ecart", nullable = false)
    @Builder.Default
    private StatutValidationEcart statutValidationEcart = StatutValidationEcart.NON_TRAITE;

    @Column(name = "commentaire_validation", columnDefinition = "TEXT")
    private String commentaireValidation;

    /** Montant effectivement impute sur le salaire du vendeur (peut differer de l'ecart brut). */
    @Column(name = "montant_impute_salaire", precision = 12, scale = 2)
    private BigDecimal montantImputeSalaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_validateur")
    private Utilisateur validateur;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @PrePersist
    public void prePersist() {
        if (dateOuverture == null) dateOuverture = LocalDateTime.now();
        if (statut == null) statut = StatutSessionCaisse.OUVERTE;
        if (statutValidationEcart == null) statutValidationEcart = StatutValidationEcart.NON_TRAITE;
    }
}
