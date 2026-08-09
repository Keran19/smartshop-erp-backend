package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parametre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Parametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Long idParametre;

    @Column(name = "cle_parametre", unique = true, length = 150)
    private String cleParametre;

    @Column(columnDefinition = "TEXT")
    private String valeur;

    @Column(columnDefinition = "TEXT")
    private String description;
}
