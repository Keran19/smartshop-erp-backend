package com.smartshop.erp.enums;

public enum TypeMouvement {
    ENTREE_APPRO, SORTIE_VENTE, PERTE, TRANSFERT, AJUSTEMENT_INVENTAIRE,
    /** Produit remis en stock suite a un retour client (remboursement ou echange). */
    RETOUR_CLIENT,
    /** Produit sorti du stock car remis au client en echange d'un retour. */
    SORTIE_ECHANGE
}
