package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventaire")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inventaire")
    private Long idInventaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @Column(name = "date_inventaire")
    private LocalDateTime dateInventaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_utilisateur", nullable = false)
    private Utilisateur utilisateur;

    @Column(columnDefinition = "TEXT")
    private String observation;

    @PrePersist
    public void prePersist() {
        if (dateInventaire == null) dateInventaire = LocalDateTime.now();
    }
}
