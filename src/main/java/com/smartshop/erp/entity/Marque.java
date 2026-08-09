package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "marque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_marque")
    private Long idMarque;

    @Column(nullable = false, unique = true, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;
}
