package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.ChangerMotDePasseRequest;
import com.smartshop.erp.dto.request.ReinitialiserMotDePasseRequest;
import com.smartshop.erp.dto.request.UtilisateurCreationRequest;
import com.smartshop.erp.dto.request.UtilisateurModificationRequest;
import com.smartshop.erp.dto.response.UtilisateurResponse;

import java.util.List;

public interface UtilisateurService {

    List<UtilisateurResponse> lister();

    UtilisateurResponse obtenir(Long id);

    /** Creation d'un compte par un ADMIN. Le nouveau compte doit changer son mot de passe a la 1ere connexion. */
    UtilisateurResponse creer(UtilisateurCreationRequest request);

    UtilisateurResponse modifier(Long id, UtilisateurModificationRequest request);

    void desactiver(Long id);

    void activer(Long id);

    /** Deverrouille manuellement un compte bloque par l'anti-bruteforce. */
    void deverrouiller(Long id);

    /** L'utilisateur change lui-meme son mot de passe (ancien mot de passe requis). */
    void changerMotDePasse(Long idUtilisateur, ChangerMotDePasseRequest request);

    /** Un ADMIN reinitialise le mot de passe d'un utilisateur (ex: mot de passe oublie). */
    void reinitialiserMotDePasse(Long idUtilisateur, ReinitialiserMotDePasseRequest request);
}
