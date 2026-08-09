package com.smartshop.erp.entity;

import jakarta.persistence.*;
import lombok.*;
import com.smartshop.erp.enums.StatutAlerte;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerte_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlerteStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alerte")
    private Long idAlerte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produit", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_boutique", nullable = false)
    private Boutique boutique;

    @Column(name = "quantite_restante", nullable = false)
    private Integer quantiteRestante;

    @Column(name = "date_alerte")
    private LocalDateTime dateAlerte;

   @Enumerated(EnumType.STRING)
   @Builder.Default
   @Column(name = "statut")
   private StatutAlerte statut = StatutAlerte.NON_LUE;
    @PrePersist
    public void prePersist() {
        if (dateAlerte == null) dateAlerte = LocalDateTime.now();
    }
}
