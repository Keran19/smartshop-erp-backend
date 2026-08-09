package com.smartshop.erp.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Detail des coupures (billets/pieces) de la zone CEMAC (XAF) declare a l'ouverture
 * ou a la fermeture de caisse. Billets en circulation : 10000, 5000, 2000, 1000, 500.
 * Les pieces (500, 100, 50, 25, 10, 5, 1 FCFA) sont saisies comme un montant total.
 */
@Data
public class DetailCoupureRequest {
    @PositiveOrZero private Integer billet10000;
    @PositiveOrZero private Integer billet5000;
    @PositiveOrZero private Integer billet2000;
    @PositiveOrZero private Integer billet1000;
    @PositiveOrZero private Integer billet500;
    @PositiveOrZero private BigDecimal pieces;
}
