package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.DetailCoupureRequest;
import com.smartshop.erp.dto.request.FermetureCaisseRequest;
import com.smartshop.erp.dto.request.OuvertureCaisseRequest;
import com.smartshop.erp.dto.request.ValidationEcartRequest;
import com.smartshop.erp.dto.response.DetailCoupureResponse;
import com.smartshop.erp.dto.response.MouvementCaisseLigne;
import com.smartshop.erp.dto.response.MouvementsCaisseResponse;
import com.smartshop.erp.dto.response.SessionCaisseAdminResponse;
import com.smartshop.erp.dto.response.SessionCaisseResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.TypeRetour;
import com.smartshop.erp.enums.StatutSessionCaisse;
import com.smartshop.erp.enums.StatutValidationEcart;
import com.smartshop.erp.enums.TypeOperationCoupure;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.CaisseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Chaque vendeur possede sa propre caisse : une session ne peut etre ouverte qu'une fois par
 * vendeur (et non par boutique), et son montant theorique tient compte de toutes les operations
 * qui font entrer ou sortir de l'argent physique du tiroir pendant qu'elle est ouverte :
 * ventes especes, remboursements de credit encaisses, acomptes recus, complements payes lors
 * d'un echange, moins les remboursements de retour et les depenses. Les ventes a credit
 * n'ajoutent volontairement RIEN au theorique (aucun argent recu) mais sont remontees a part,
 * de maniere informative, pour que l'ecart s'explique meme quand une partie du chiffre du jour
 * n'a pas encore ete encaissee.
 */
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
    private final RetourRepository retourRepository;
    private final PaiementCreditRepository paiementCreditRepository;
    private final VersementAcompteRepository versementAcompteRepository;

    @Override
    @Transactional
    public SessionCaisseResponse ouvrir(OuvertureCaisseRequest request, Long idUtilisateurConnecte) {
        sessionCaisseRepository.findByUtilisateur_IdUtilisateurAndStatut(idUtilisateurConnecte, StatutSessionCaisse.OUVERTE)
                .ifPresent(s -> { throw new OperationInvalideException("Vous avez deja une session de caisse ouverte (id=" + s.getIdSession() + "). Fermez-la avant d'en ouvrir une nouvelle."); });

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

        MouvementsCaisseResponse mvts = calculerMouvements(session, maintenant);

        session.setDateFermeture(maintenant);
        session.setMontantTheorique(mvts.getMontantTheoriqueCourant());
        session.setMontantCompte(montantCompte);
        session.setEcart(montantCompte.subtract(mvts.getMontantTheoriqueCourant()));
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
    public SessionCaisseResponse sessionOuverteParVendeur(Long idUtilisateur) {
        SessionCaisse session = sessionCaisseRepository.findByUtilisateur_IdUtilisateurAndStatut(idUtilisateur, StatutSessionCaisse.OUVERTE)
                .orElseThrow(() -> new RessourceNonTrouveeException("Aucune session de caisse ouverte pour cet utilisateur"));
        return construireReponse(session);
    }

    @Override
    public List<SessionCaisseResponse> historiqueParBoutique(Long idBoutique) {
        return sessionCaisseRepository.findByBoutique_IdBoutiqueOrderByDateOuvertureDesc(idBoutique).stream()
                .map(this::construireReponse).collect(Collectors.toList());
    }

    @Override
    public List<SessionCaisseResponse> historiqueParVendeur(Long idUtilisateur) {
        return sessionCaisseRepository.findByUtilisateur_IdUtilisateurOrderByDateOuvertureDesc(idUtilisateur).stream()
                .map(this::construireReponse).collect(Collectors.toList());
    }

    @Override
    public MouvementsCaisseResponse mouvements(Long idSession) {
        SessionCaisse session = trouver(idSession);
        LocalDateTime fin = session.getDateFermeture() != null ? session.getDateFermeture() : LocalDateTime.now();
        return calculerMouvements(session, fin);
    }

    @Override
    public List<MouvementCaisseLigne> journal(Long idSession) {
        SessionCaisse session = trouver(idSession);
        Long idVendeur = session.getUtilisateur().getIdUtilisateur();
        LocalDateTime debut = session.getDateOuverture();
        LocalDateTime fin = session.getDateFermeture() != null ? session.getDateFermeture() : LocalDateTime.now();

        List<MouvementCaisseLigne> lignes = new java.util.ArrayList<>();

        for (Vente v : venteRepository.listeParVendeurEtPeriode(idVendeur, debut, fin)) {
            boolean credit = v.getModeReglement() == com.smartshop.erp.enums.ModeReglement.CREDIT;
            lignes.add(MouvementCaisseLigne.builder()
                    .type(credit ? "VENTE_CREDIT" : "VENTE_ESPECES")
                    .date(v.getDateVente())
                    .reference(v.getNumeroVente())
                    .libelle(credit ? "Vente a credit" : "Vente comptant")
                    .montant(credit ? BigDecimal.ZERO : v.getMontantFinal())
                    .build());
        }

        for (PaiementCredit p : paiementCreditRepository.listeParVendeurEtPeriode(idVendeur, debut, fin)) {
            lignes.add(MouvementCaisseLigne.builder()
                    .type("REMBOURSEMENT_CREDIT")
                    .date(p.getDatePaiement())
                    .reference(p.getCredit().getVente().getNumeroVente())
                    .libelle("Remboursement credit - " + p.getCredit().getClient().getNom())
                    .montant(p.getMontant())
                    .build());
        }

        for (VersementAcompte a : versementAcompteRepository.listeParVendeurEtPeriode(idVendeur, debut, fin)) {
            lignes.add(MouvementCaisseLigne.builder()
                    .type("ACOMPTE")
                    .date(a.getDateVersement())
                    .reference(a.getAcompte().getNumeroAcompte())
                    .libelle("Acompte recu - " + a.getAcompte().getClient().getNom())
                    .montant(a.getMontant())
                    .build());
        }

        for (Retour r : retourRepository.listeParVendeurEtPeriode(idVendeur, debut, fin)) {
            String libelleType = r.getTypeRetour() == TypeRetour.REMBOURSEMENT ? "Retour rembourse"
                    : r.getTypeRetour() == TypeRetour.ECHANGE_MEME_VALEUR ? "Echange (valeur egale)"
                    : "Echange (valeur differente)";
            // Impact net : le complement recu (positif) moins le remboursement verse (negatif).
            BigDecimal impact = r.getMontantComplement().subtract(r.getMontantRembourse());
            lignes.add(MouvementCaisseLigne.builder()
                    .type("RETOUR")
                    .date(r.getDateRetour())
                    .reference(r.getNumeroRetour())
                    .libelle(libelleType + " - vente " + r.getVente().getNumeroVente())
                    .montant(impact)
                    .build());
        }

        for (Depense d : depenseRepository.listeParVendeurEtPeriode(idVendeur, debut, fin)) {
            lignes.add(MouvementCaisseLigne.builder()
                    .type("DEPENSE")
                    .date(d.getDateDepense())
                    .reference(d.getCategorie())
                    .libelle("Depense - " + d.getLibelle())
                    .montant(d.getMontant().negate())
                    .build());
        }

        lignes.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return lignes;
    }

    @Override
    public List<SessionCaisseAdminResponse> sessionsAdmin(LocalDate jour) {
        LocalDate journee = jour != null ? jour : LocalDate.now();
        LocalDateTime debutJour = journee.atStartOfDay();
        LocalDateTime finJour = journee.atTime(LocalTime.MAX);

        return sessionCaisseRepository.findByDateOuvertureBetweenOrderByDateOuvertureDesc(debutJour, finJour).stream()
                .map(this::versReponseAdmin)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SessionCaisseAdminResponse validerEcart(Long idSession, ValidationEcartRequest request, Long idAdminConnecte) {
        SessionCaisse session = trouver(idSession);
        if (session.getStatut() != StatutSessionCaisse.FERMEE) {
            throw new OperationInvalideException("On ne peut valider l'ecart que d'une caisse deja fermee");
        }
        Utilisateur admin = utilisateurRepository.findById(idAdminConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        session.setStatutValidationEcart(request.getStatut());
        session.setCommentaireValidation(request.getCommentaire());
        session.setValidateur(admin);
        session.setDateValidation(LocalDateTime.now());

        if (request.getStatut() == StatutValidationEcart.IMPUTE_SALAIRE) {
            BigDecimal montant = request.getMontantImpute() != null
                    ? request.getMontantImpute()
                    : session.getEcart().abs();
            session.setMontantImputeSalaire(montant);
        } else {
            session.setMontantImputeSalaire(null);
        }

        session = sessionCaisseRepository.save(session);
        return versReponseAdmin(session);
    }

    // ---------------------------------------------------------------

    private MouvementsCaisseResponse calculerMouvements(SessionCaisse session, LocalDateTime fin) {
        Long idVendeur = session.getUtilisateur().getIdUtilisateur();
        LocalDateTime debut = session.getDateOuverture();

        BigDecimal ventesEspeces = nz(venteRepository.sommeVentesComptantParVendeurEtPeriode(idVendeur, debut, fin));
        BigDecimal ventesCredit = nz(venteRepository.sommeVentesCreditParVendeurEtPeriode(idVendeur, debut, fin));
        long nombreVentes = venteRepository.nombreVentesParVendeurEtPeriode(idVendeur, debut, fin);
        BigDecimal remboursementsCredit = nz(paiementCreditRepository.sommeParVendeurEtPeriode(idVendeur, debut, fin));
        BigDecimal acomptesRecus = nz(versementAcompteRepository.sommeParVendeurEtPeriode(idVendeur, debut, fin));
        BigDecimal retoursRembourses = nz(retourRepository.sommeRembourseParVendeurEtPeriode(idVendeur, debut, fin));
        BigDecimal retoursComplements = nz(retourRepository.sommeComplementParVendeurEtPeriode(idVendeur, debut, fin));
        BigDecimal depenses = nz(depenseRepository.sommeParVendeurEtPeriode(idVendeur, debut, fin));

        BigDecimal montantTheorique = session.getFondCaisse()
                .add(ventesEspeces)
                .add(remboursementsCredit)
                .add(acomptesRecus)
                .add(retoursComplements)
                .subtract(retoursRembourses)
                .subtract(depenses);

        return MouvementsCaisseResponse.builder()
                .idSession(session.getIdSession())
                .fondCaisse(session.getFondCaisse())
                .ventesEspeces(ventesEspeces)
                .nombreVentes(nombreVentes)
                .ventesCredit(ventesCredit)
                .remboursementsCredit(remboursementsCredit)
                .acomptesRecus(acomptesRecus)
                .retoursRembourses(retoursRembourses)
                .retoursComplements(retoursComplements)
                .depenses(depenses)
                .montantTheoriqueCourant(montantTheorique)
                .build();
    }

    private BigDecimal nz(BigDecimal valeur) {
        return valeur == null ? BigDecimal.ZERO : valeur;
    }

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

        LocalDateTime fin = session.getDateFermeture() != null ? session.getDateFermeture() : LocalDateTime.now();
        BigDecimal creditNonEncaisse = nz(venteRepository.sommeVentesCreditParVendeurEtPeriode(
                session.getUtilisateur().getIdUtilisateur(), session.getDateOuverture(), fin));

        return SessionCaisseResponse.builder()
                .idSession(session.getIdSession())
                .idBoutique(session.getBoutique().getIdBoutique())
                .boutique(session.getBoutique().getNom())
                .idUtilisateur(session.getUtilisateur().getIdUtilisateur())
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
                .creditNonEncaisse(creditNonEncaisse)
                .build();
    }

    private DetailCoupureResponse versDetailReponse(DetailCoupureSession d) {
        return DetailCoupureResponse.builder()
                .billet10000(d.getBillet10000()).billet5000(d.getBillet5000()).billet2000(d.getBillet2000())
                .billet1000(d.getBillet1000()).billet500(d.getBillet500()).pieces(d.getPieces()).total(d.getTotal())
                .build();
    }

    private SessionCaisseAdminResponse versReponseAdmin(SessionCaisse session) {
        LocalDateTime fin = session.getDateFermeture() != null ? session.getDateFermeture() : LocalDateTime.now();
        MouvementsCaisseResponse mvts = calculerMouvements(session, fin);

        return SessionCaisseAdminResponse.builder()
                .idSession(session.getIdSession())
                .boutique(session.getBoutique().getNom())
                .vendeur(session.getUtilisateur().getNom() + " " + session.getUtilisateur().getPrenom())
                .dateOuverture(session.getDateOuverture())
                .dateFermeture(session.getDateFermeture())
                .statut(session.getStatut())
                .fondCaisse(session.getFondCaisse())
                .montantVenteEspeces(mvts.getVentesEspeces())
                .montantVenteCredit(mvts.getVentesCredit())
                .depensesJournee(mvts.getDepenses())
                .montantTheoriqueAttendu(session.getStatut() == StatutSessionCaisse.FERMEE
                        ? session.getMontantTheorique() : mvts.getMontantTheoriqueCourant())
                .montantRenseigne(session.getMontantCompte())
                .ecart(session.getEcart())
                .statutValidationEcart(session.getStatutValidationEcart())
                .commentaireValidation(session.getCommentaireValidation())
                .montantImputeSalaire(session.getMontantImputeSalaire())
                .validateur(session.getValidateur() != null
                        ? session.getValidateur().getNom() + " " + session.getValidateur().getPrenom() : null)
                .dateValidation(session.getDateValidation())
                .build();
    }
}
