package com.smartshop.erp.entity;

import com.smartshop.erp.enums.TypeOperationCoupure;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Detail des coupures (billets/pieces) de la zone CEMAC (XAF)
 * declarees a l'ouverture ET a la fermeture d'une session de caisse.
 * Billets CEMAC en circulation : 10000, 5000, 2000, 1000, 500.
 * Les pieces sont saisies sous forme de montant total (100, 50, 25, 10, 5, 1 FCFA etc).
 */
@Entity
@Table(name = "detail_coupure_session")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetailCoupureSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detail")
    private Long idDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_session", nullable = false)
    private SessionCaisse session;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", nullable = false, length = 20)
    private TypeOperationCoupure typeOperation;

    @Column(name = "billet_10000")
    @Builder.Default
    private Integer billet10000 = 0;

    @Column(name = "billet_5000")
    @Builder.Default
    private Integer billet5000 = 0;

    @Column(name = "billet_2000")
    @Builder.Default
    private Integer billet2000 = 0;

    @Column(name = "billet_1000")
    @Builder.Default
    private Integer billet1000 = 0;

    @Column(name = "billet_500")
    @Builder.Default
    private Integer billet500 = 0;

    // Montant total des pieces (100, 50, 25, 10, 5, 1 FCFA)
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pieces = BigDecimal.ZERO;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal total;
}
