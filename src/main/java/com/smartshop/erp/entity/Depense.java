package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "depense")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Depense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_depense")
    private Long idDepense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(length = 100)
    private String categorie;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @Column(name = "date_depense")
    private LocalDateTime dateDepense;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @PrePersist
    public void prePersist() {
        if (dateDepense == null) dateDepense = LocalDateTime.now();
    }
}
