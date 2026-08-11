package com.smartshop.erp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Une ligne individuelle du journal de caisse (par opposition aux totaux de MouvementsCaisseResponse).
 * Permet de voir le detail exact : quel retour, quel remboursement, quelle depense, a quelle heure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MouvementCaisseLigne {
    private String type; // VENTE_ESPECES, VENTE_CREDIT, REMBOURSEMENT_CREDIT, ACOMPTE, RETOUR, DEPENSE
    private LocalDateTime date;
    private String reference;   // numero de vente / retour / observation
    private String libelle;     // description lisible
    /** Positif = entre en caisse, negatif = sort de la caisse. */
    private BigDecimal montant;
}
