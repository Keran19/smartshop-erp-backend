package com.smartshop.erp.enums;

public enum StatutValidationEcart {
    /** Caisse fermee, ecart pas encore examine par un admin/gerant. */
    NON_TRAITE,
    /** L'ecart a ete examine et accepte tel quel (ex: petite erreur de rendu de monnaie). */
    VALIDE,
    /** L'ecart a ete impute (deduit) sur le salaire du vendeur concerne. */
    IMPUTE_SALAIRE
}
