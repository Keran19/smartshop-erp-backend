package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categorie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categorie")
    private Long idCategorie;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}
