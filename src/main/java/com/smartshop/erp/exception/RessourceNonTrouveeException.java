package com.smartshop.erp.exception;

/** Levee quand une ressource demandee (produit, client, vente...) n'existe pas. */
public class RessourceNonTrouveeException extends RuntimeException {
    public RessourceNonTrouveeException(String message) {
        super(message);
    }
}
