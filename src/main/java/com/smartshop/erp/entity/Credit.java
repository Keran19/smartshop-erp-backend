package com.smartshop.erp.entity;

import com.smartshop.erp.enums.StatutCredit;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_credit")
    private Long idCredit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false, unique = true)
    private Vente vente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    private Client client;

    @Column(name = "montant_initial", nullable = false, precision = 12, scale = 2)
    private BigDecimal montantInitial;

    @Column(name = "montant_paye", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "reste_a_payer", nullable = false, precision = 12, scale = 2)
    private BigDecimal resteAPayer;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_limite")
    private LocalDate dateLimite;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutCredit statut = StatutCredit.EN_COURS;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (statut == null) statut = StatutCredit.EN_COURS;
        if (montantPaye == null) montantPaye = BigDecimal.ZERO;
    }
}
