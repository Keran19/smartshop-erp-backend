package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "facture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_facture")
    private Long idFacture;

    @Column(name = "numero_facture", nullable = false, unique = true, length = 50)
    private String numeroFacture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vente", nullable = false, unique = true)
    private Vente vente;

    @Column(name = "date_impression")
    private LocalDateTime dateImpression;

    @Builder.Default
    private Boolean imprimee = false;

    @PrePersist
    public void prePersist() {
        if (dateImpression == null) dateImpression = LocalDateTime.now();
        if (imprimee == null) imprimee = false;
    }
}
