package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.response.SessionCaisseResponse;

import java.util.List;

public interface CaisseService {

    /** Ouvre une session de caisse pour une boutique, en declarant le fond de caisse par coupures. */
    SessionCaisseResponse ouvrir(OuvertureCaisseRequest request, Long idUtilisateurConnecte);

    /** Ferme la session, en declarant le montant compte par coupures ; calcule l'ecart automatiquement. */
    SessionCaisseResponse fermer(Long idSession, FermetureCaisseRequest request);

    SessionCaisseResponse obtenir(Long idSession);

    /** La session actuellement ouverte pour une boutique, s'il y en a une. */
    SessionCaisseResponse sessionOuverte(Long idBoutique);

    List<SessionCaisseResponse> historique(Long idBoutique);
}
