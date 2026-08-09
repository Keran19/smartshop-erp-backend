package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "versement_acompte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VersementAcompte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_versement")
    private Long idVersement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_acompte", nullable = false)
    private Acompte acompte;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_versement")
    private LocalDateTime dateVersement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @PrePersist
    public void prePersist() {
        if (dateVersement == null) dateVersement = LocalDateTime.now();
    }
}
