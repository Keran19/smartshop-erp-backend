package com.smartshop.erp.service;

import com.smartshop.erp.entity.Utilisateur;

/**
 * Gere le compteur de tentatives de connexion echouees et le verrouillage temporaire
 * d'un compte apres trop d'echecs consecutifs (protection anti-bruteforce).
 */
public interface CompteSecuriteService {

    /** A appeler apres une authentification reussie : remet le compteur a zero et deverrouille. */
    void reinitialiserApresSucces(Utilisateur utilisateur);

    /**
     * A appeler apres un echec d'authentification : incremente le compteur et, si le seuil
     * est atteint, verrouille le compte pour la duree configuree.
     */
    void enregistrerEchec(Utilisateur utilisateur);
}
