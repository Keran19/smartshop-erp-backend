package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "client")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_client")
    private Long idClient;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(length = 150)
    private String prenom;

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
