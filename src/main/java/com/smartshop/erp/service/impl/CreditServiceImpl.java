package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.PaiementCreditRequest;
import com.smartshop.erp.dto.response.CreditResponse;
import com.smartshop.erp.dto.response.PaiementCreditResponse;
import com.smartshop.erp.entity.Credit;
import com.smartshop.erp.entity.PaiementCredit;
import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.enums.StatutCredit;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.CreditRepository;
import com.smartshop.erp.repository.PaiementCreditRepository;
import com.smartshop.erp.repository.UtilisateurRepository;
import com.smartshop.erp.service.CreditService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditServiceImpl implements CreditService {

    private final CreditRepository creditRepository;
    private final PaiementCreditRepository paiementCreditRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityManager entityManager;

    @Override
    public List<CreditResponse> lister(StatutCredit statut) {
        List<Credit> credits = statut != null ? creditRepository.findByStatut(statut) : creditRepository.findAll();
        return credits.stream().map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    public CreditResponse obtenir(Long idCredit) {
        return versReponse(trouver(idCredit));
    }

    @Override
    public List<CreditResponse> parClient(Long idClient) {
        return creditRepository.findByClient_IdClient(idClient).stream().map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CreditResponse enregistrerPaiement(Long idCredit, PaiementCreditRequest request, Long idUtilisateurConnecte) {
        Credit credit = trouver(idCredit);

        if (credit.getStatut() == StatutCredit.SOLDE) {
            throw new OperationInvalideException("Ce credit est deja solde");
        }
        if (request.getMontant().compareTo(credit.getResteAPayer()) > 0) {
            throw new OperationInvalideException("Le montant verse (" + request.getMontant()
                    + ") depasse le reste a payer (" + credit.getResteAPayer() + ")");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        paiementCreditRepository.save(PaiementCredit.builder()
                .credit(credit)
                .montant(request.getMontant())
                .observation(request.getObservation())
                .utilisateur(utilisateur)
                .build());

        // Le trigger SQL trg_credit met a jour montant_paye/reste_a_payer/statut ; Hibernate
        // ignore ces changements faits hors de son contexte de persistance, d'ou le refresh.
        entityManager.flush();
        entityManager.refresh(credit);

        return versReponse(credit);
    }

    // ---------------------------------------------------------------

    private Credit trouver(Long idCredit) {
        return creditRepository.findById(idCredit)
                .orElseThrow(() -> new RessourceNonTrouveeException("Credit introuvable, id=" + idCredit));
    }

    private CreditResponse versReponse(Credit credit) {
        List<PaiementCreditResponse> paiements = paiementCreditRepository
                .findByCredit_IdCreditOrderByDatePaiementDesc(credit.getIdCredit()).stream()
                .map(p -> PaiementCreditResponse.builder()
                        .idPaiement(p.getIdPaiement())
                        .montant(p.getMontant())
                        .datePaiement(p.getDatePaiement())
                        .utilisateur(p.getUtilisateur() != null ? p.getUtilisateur().getNom() + " " + p.getUtilisateur().getPrenom() : null)
                        .observation(p.getObservation())
                        .build())
                .collect(Collectors.toList());

        return CreditResponse.builder()
                .idCredit(credit.getIdCredit())
                .idVente(credit.getVente().getIdVente())
                .numeroVente(credit.getVente().getNumeroVente())
                .idClient(credit.getClient().getIdClient())
                .client(credit.getClient().getNom() + " " + (credit.getClient().getPrenom() != null ? credit.getClient().getPrenom() : ""))
                .telephoneClient(credit.getClient().getTelephone())
                .montantInitial(credit.getMontantInitial())
                .montantPaye(credit.getMontantPaye())
                .resteAPayer(credit.getResteAPayer())
                .dateCreation(credit.getDateCreation())
                .dateLimite(credit.getDateLimite())
                .statut(credit.getStatut())
                .observation(credit.getObservation())
                .paiements(paiements)
                .build();
    }
}
