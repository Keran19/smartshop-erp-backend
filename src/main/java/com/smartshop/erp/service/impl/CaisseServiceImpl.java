package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.DetailCoupureRequest;
import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.response.DetailCoupureResponse;
import com.smartshop.erp.dto.response.SessionCaisseResponse;
import com.smartshop.erp.entity.Boutique;
import com.smartshop.erp.entity.DetailCoupureSession;
import com.smartshop.erp.entity.SessionCaisse;
import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.enums.StatutSessionCaisse;
import com.smartshop.erp.enums.TypeOperationCoupure;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.CaisseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaisseServiceImpl implements CaisseService {

    private static final BigDecimal V_10000 = BigDecimal.valueOf(10000);
    private static final BigDecimal V_5000 = BigDecimal.valueOf(5000);
    private static final BigDecimal V_2000 = BigDecimal.valueOf(2000);
    private static final BigDecimal V_1000 = BigDecimal.valueOf(1000);
    private static final BigDecimal V_500 = BigDecimal.valueOf(500);

    private final SessionCaisseRepository sessionCaisseRepository;
    private final DetailCoupureSessionRepository detailCoupureSessionRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final VenteRepository venteRepository;
    private final DepenseRepository depenseRepository;

    @Override
    @Transactional
    public SessionCaisseResponse ouvrir(OuvertureCaisseRequest request, Long idUtilisateurConnecte) {
        sessionCaisseRepository.findByBoutique_IdBoutiqueAndStatut(request.getIdBoutique(), StatutSessionCaisse.OUVERTE)
                .ifPresent(s -> { throw new OperationInvalideException("Une session de caisse est deja ouverte pour cette boutique (id=" + s.getIdSession() + ")"); });

        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        BigDecimal total = calculerTotal(request.getCoupures());

        SessionCaisse session = SessionCaisse.builder()
                .boutique(boutique)
                .utilisateur(utilisateur)
                .dateOuverture(LocalDateTime.now())
                .fondCaisse(total)
                .statut(StatutSessionCaisse.OUVERTE)
                .observation(request.getObservation())
                .build();
        session = sessionCaisseRepository.save(session);

        detailCoupureSessionRepository.save(construireDetail(session, TypeOperationCoupure.OUVERTURE, request.getCoupures(), total));

        return construireReponse(session);
    }

    @Override
    @Transactional
    public SessionCaisseResponse fermer(Long idSession, FermetureCaisseRequest request) {
        SessionCaisse session = trouver(idSession);
        if (session.getStatut() != StatutSessionCaisse.OUVERTE) {
            throw new OperationInvalideException("Cette session de caisse est deja fermee");
        }

        LocalDateTime maintenant = LocalDateTime.now();
        BigDecimal montantCompte = calculerTotal(request.getCoupures());

        Long idBoutique = session.getBoutique().getIdBoutique();
        BigDecimal ventesEspeces = venteRepository.sommeVentesComptantParBoutiqueEtPeriode(idBoutique, session.getDateOuverture(), maintenant);
        BigDecimal depenses = depenseRepository.sommeParBoutiqueEtPeriode(idBoutique, session.getDateOuverture(), maintenant);
        BigDecimal montantTheorique = session.getFondCaisse()
                .add(ventesEspeces == null ? BigDecimal.ZERO : ventesEspeces)
                .subtract(depenses == null ? BigDecimal.ZERO : depenses);

        session.setDateFermeture(maintenant);
        session.setMontantTheorique(montantTheorique);
        session.setMontantCompte(montantCompte);
        session.setEcart(montantCompte.subtract(montantTheorique));
        session.setStatut(StatutSessionCaisse.FERMEE);
        if (request.getObservation() != null) {
            String obsExistante = session.getObservation();
            session.setObservation(obsExistante == null ? request.getObservation() : obsExistante + " | Fermeture : " + request.getObservation());
        }
        session = sessionCaisseRepository.save(session);

        detailCoupureSessionRepository.save(construireDetail(session, TypeOperationCoupure.FERMETURE, request.getCoupures(), montantCompte));

        return construireReponse(session);
    }

    @Override
    public SessionCaisseResponse obtenir(Long idSession) {
        return construireReponse(trouver(idSession));
    }

    @Override
    public SessionCaisseResponse sessionOuverte(Long idBoutique) {
        SessionCaisse session = sessionCaisseRepository.findByBoutique_IdBoutiqueAndStatut(idBoutique, StatutSessionCaisse.OUVERTE)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune session de caisse ouverte pour cette boutique"));
        return construireReponse(session);
    }

    @Override
    public List<SessionCaisseResponse> historique(Long idBoutique) {
        return sessionCaisseRepository.findByBoutique_IdBoutiqueOrderByDateOuvertureDesc(idBoutique).stream()
                .map(this::construireReponse).collect(Collectors.toList());
    }

    // ---------------------------------------------------------------

    private BigDecimal calculerTotal(DetailCoupureRequest c) {
        BigDecimal total = BigDecimal.ZERO;
        if (c.getBillet10000() != null) total = total.add(V_10000.multiply(BigDecimal.valueOf(c.getBillet10000())));
        if (c.getBillet5000() != null) total = total.add(V_5000.multiply(BigDecimal.valueOf(c.getBillet5000())));
        if (c.getBillet2000() != null) total = total.add(V_2000.multiply(BigDecimal.valueOf(c.getBillet2000())));
        if (c.getBillet1000() != null) total = total.add(V_1000.multiply(BigDecimal.valueOf(c.getBillet1000())));
        if (c.getBillet500() != null) total = total.add(V_500.multiply(BigDecimal.valueOf(c.getBillet500())));
        if (c.getPieces() != null) total = total.add(c.getPieces());
        return total;
    }

    private DetailCoupureSession construireDetail(SessionCaisse session, TypeOperationCoupure type, DetailCoupureRequest c, BigDecimal total) {
        return DetailCoupureSession.builder()
                .session(session)
                .typeOperation(type)
                .billet10000(c.getBillet10000() == null ? 0 : c.getBillet10000())
                .billet5000(c.getBillet5000() == null ? 0 : c.getBillet5000())
                .billet2000(c.getBillet2000() == null ? 0 : c.getBillet2000())
                .billet1000(c.getBillet1000() == null ? 0 : c.getBillet1000())
                .billet500(c.getBillet500() == null ? 0 : c.getBillet500())
                .pieces(c.getPieces() == null ? BigDecimal.ZERO : c.getPieces())
                .total(total)
                .build();
    }

    private SessionCaisse trouver(Long idSession) {
        return sessionCaisseRepository.findById(idSession)
                .orElseThrow(() -> new RessourceNonTrouveeException("Session de caisse introuvable, id=" + idSession));
    }

    private SessionCaisseResponse construireReponse(SessionCaisse session) {
        DetailCoupureResponse ouverture = detailCoupureSessionRepository
                .findBySession_IdSessionAndTypeOperation(session.getIdSession(), TypeOperationCoupure.OUVERTURE)
                .map(this::versDetailReponse).orElse(null);
        DetailCoupureResponse fermeture = detailCoupureSessionRepository
                .findBySession_IdSessionAndTypeOperation(session.getIdSession(), TypeOperationCoupure.FERMETURE)
                .map(this::versDetailReponse).orElse(null);

        return SessionCaisseResponse.builder()
                .idSession(session.getIdSession())
                .idBoutique(session.getBoutique().getIdBoutique())
                .boutique(session.getBoutique().getNom())
                .utilisateur(session.getUtilisateur().getNom() + " " + session.getUtilisateur().getPrenom())
                .dateOuverture(session.getDateOuverture())
                .dateFermeture(session.getDateFermeture())
                .fondCaisse(session.getFondCaisse())
                .montantTheorique(session.getMontantTheorique())
                .montantCompte(session.getMontantCompte())
                .ecart(session.getEcart())
                .statut(session.getStatut())
                .observation(session.getObservation())
                .coupureOuverture(ouverture)
                .coupureFermeture(fermeture)
                .build();
    }

    private DetailCoupureResponse versDetailReponse(DetailCoupureSession d) {
        return DetailCoupureResponse.builder()
                .billet10000(d.getBillet10000()).billet5000(d.getBillet5000()).billet2000(d.getBillet2000())
                .billet1000(d.getBillet1000()).billet500(d.getBillet500()).pieces(d.getPieces()).total(d.getTotal())
                .build();
    }
}
