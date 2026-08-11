package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Photo en temps reel de la session de caisse en cours (ou fermee) : toutes les operations
 * qui l'affectent (ventes especes/credit, remboursements de credit, acomptes, retours,
 * depenses) et le montant theorique qui en decoule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementsCaisseResponse {
    private Long idSession;
    private BigDecimal fondCaisse;

    private BigDecimal ventesEspeces;
    private long nombreVentes;

    /** Informatif uniquement : montant vendu a credit, non encaisse, ne doit pas compter dans le theorique. */
    private BigDecimal ventesCredit;

    private BigDecimal remboursementsCredit;
    private BigDecimal acomptesRecus;

    private BigDecimal retoursRembourses;
    private BigDecimal retoursComplements;

    private BigDecimal depenses;

    /** fondCaisse + ventesEspeces + remboursementsCredit + acomptesRecus + retoursComplements - retoursRembourses - depenses */
    private BigDecimal montantTheoriqueCourant;
}
