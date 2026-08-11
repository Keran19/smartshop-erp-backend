package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.request.ValidationEcartRequest;
import com.smartshop.erp.dto.response.MouvementCaisseLigne;
import com.smartshop.erp.dto.response.MouvementsCaisseResponse;
import com.smartshop.erp.dto.response.SessionCaisseAdminResponse;
import com.smartshop.erp.dto.response.SessionCaisseResponse;

import java.time.LocalDate;
import java.util.List;

public interface CaisseService {

    SessionCaisseResponse ouvrir(OuvertureCaisseRequest request, Long idUtilisateurConnecte);

    SessionCaisseResponse fermer(Long idSession, FermetureCaisseRequest request);

    SessionCaisseResponse obtenir(Long idSession);

    /** La session actuellement ouverte pour CE vendeur (chaque vendeur a sa propre caisse). */
    SessionCaisseResponse sessionOuverteParVendeur(Long idUtilisateur);

    List<SessionCaisseResponse> historiqueParBoutique(Long idBoutique);

    List<SessionCaisseResponse> historiqueParVendeur(Long idUtilisateur);

    /** Photo en temps reel des mouvements (ventes, credits, retours, acomptes, depenses) de la session. */
    MouvementsCaisseResponse mouvements(Long idSession);

    /** Journal detaille : une ligne par operation individuelle (retour, remboursement, acompte, depense, vente). */
    List<MouvementCaisseLigne> journal(Long idSession);

    /** Toutes les sessions (toutes boutiques, tous vendeurs) ouvertes le jour donne, pour le tableau de bord admin. */
    List<SessionCaisseAdminResponse> sessionsAdmin(LocalDate jour);

    /** Valide ou impute sur salaire l'ecart d'une session fermee. */
    SessionCaisseAdminResponse validerEcart(Long idSession, ValidationEcartRequest request, Long idAdminConnecte);
}
