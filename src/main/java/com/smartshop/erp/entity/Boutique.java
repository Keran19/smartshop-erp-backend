package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "boutique")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boutique {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_boutique")
    private Long idBoutique;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 255)
    private String adresse;

    @Column(length = 30)
    private String telephone;

    @Builder.Default
    private Boolean principale = false;

    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) dateCreation = LocalDateTime.now();
        if (actif == null) actif = true;
        if (principale == null) principale = false;
    }
}
