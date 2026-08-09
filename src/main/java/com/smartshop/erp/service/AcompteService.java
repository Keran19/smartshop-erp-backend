package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.AcompteRequest;
import com.smartshop.erp.dto.request.VersementAcompteRequest;
import com.smartshop.erp.dto.response.AcompteResponse;
import com.smartshop.erp.enums.StatutAcompte;

import java.util.List;

public interface AcompteService {

    List<AcompteResponse> lister(StatutAcompte statut, Long idBoutique);

    AcompteResponse obtenir(Long idAcompte);

    List<AcompteResponse> parClient(Long idClient);

    /** Cree un acompte. Le client doit deja exister (voir ClientController#obtenirParTelephone
     * pour verifier son existence avant appel, et rediriger vers sa creation si necessaire). */
    AcompteResponse creer(AcompteRequest request, Long idVendeurConnecte);

    /** Enregistre un versement sur un acompte existant ; met a jour automatiquement le solde. */
    AcompteResponse enregistrerVersement(Long idAcompte, VersementAcompteRequest request, Long idUtilisateurConnecte);

    void annuler(Long idAcompte);
}
