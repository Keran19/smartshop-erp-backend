package com.smartshop.erp.entity;

import com.smartshop.erp.enums.StatutRetour;
import com.smartshop.erp.enums.TypeRetour;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Retour client rattache a une vente d'origine. Trois cas d'usage :
 *  - REMBOURSEMENT              : produits rendus, argent restitue, pas de nouveaux produits.
 *  - ECHANGE_MEME_VALEUR        : produits rendus contre d'autres produits de valeur identique.
 *  - ECHANGE_VALEUR_DIFFERENTE  : produits rendus contre d'autres produits de valeur differente
 *                                 (le client paie un complement, ou se fait rembourser la difference).
 * Le retour est un document independant : la vente d'origine n'est jamais modifiee, ce qui
 * preserve l'integrite de l'historique de vente.
 */
@Entity
@Table(name = "retour")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Retour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_retour")
    private Long idRetour;

    @Column(name = "numero_retour", nullable = false, unique = true, length = 50)
    private String numeroRetour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_retour", nullable = false, length = 30)
    private TypeRetour typeRetour;

    /** Valeur totale des produits rendus (calculee au prix auquel ils avaient ete vendus). */
    @Column(name = "montant_retourne", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRetourne = BigDecimal.ZERO;

    /** Valeur totale des nouveaux produits donnes en echange (0 pour un simple remboursement). */
    @Column(name = "montant_echange", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantEchange = BigDecimal.ZERO;

    /** Somme effectivement remboursee au client (remboursement pur, ou difference en sa faveur). */
    @Column(name = "montant_rembourse", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantRembourse = BigDecimal.ZERO;

    /** Somme que le client doit payer en plus (echange vers des produits de plus grande valeur). */
    @Column(name = "montant_complement", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantComplement = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutRetour statut = StatutRetour.VALIDE;

    @Column(name = "date_retour")
    private LocalDateTime dateRetour;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @OneToMany(mappedBy = "retour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneRetour> lignesRetour = new ArrayList<>();

    @OneToMany(mappedBy = "retour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneEchange> lignesEchange = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (dateRetour == null) dateRetour = LocalDateTime.now();
        if (statut == null) statut = StatutRetour.VALIDE;
    }
}
