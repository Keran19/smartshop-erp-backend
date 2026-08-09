package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fournisseur")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fournisseur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fournisseur")
    private Long idFournisseur;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 30)
    private String telephone;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String adresse;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (actif == null) actif = true;
    }
}
