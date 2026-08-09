package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiement_credit")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaiementCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_paiement")
    private Long idPaiement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_credit", nullable = false)
    private Credit credit;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (datePaiement == null) datePaiement = LocalDateTime.now();
    }
}
