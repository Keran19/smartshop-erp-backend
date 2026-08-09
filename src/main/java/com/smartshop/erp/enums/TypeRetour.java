package com.smartshop.erp.enums;

public enum TypeRetour {
    /** Le client rapporte des produits et se fait rembourser. */
    REMBOURSEMENT,
    /** Le client echange des produits contre d'autres de meme valeur totale (aucun paiement). */
    ECHANGE_MEME_VALEUR,
    /** Le client echange des produits contre d'autres de valeur differente (complement a payer ou a rendre). */
    ECHANGE_VALEUR_DIFFERENTE
}
