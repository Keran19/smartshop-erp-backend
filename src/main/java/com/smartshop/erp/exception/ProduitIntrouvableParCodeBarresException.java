package com.smartshop.erp.exception;

/**
 * Levee specifiquement lors du scan d'un code-barres qui ne correspond a aucun produit.
 * Le controller la traduit en reponse 404 avec un code metier dedie ("PRODUIT_INCONNU")
 * afin que le frontend puisse rediriger automatiquement vers l'ecran d'ajout de produit,
 * en pre-remplissant le code-barres scanne.
 */
public class ProduitIntrouvableParCodeBarresException extends RuntimeException {
    private final String codeBarres;

    public ProduitIntrouvableParCodeBarresException(String codeBarres) {
        super("Aucun produit ne correspond au code-barres : " + codeBarres);
        this.codeBarres = codeBarres;
    }

    public String getCodeBarres() {
        return codeBarres;
    }
}
