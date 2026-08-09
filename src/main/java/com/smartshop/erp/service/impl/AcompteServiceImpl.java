package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.AcompteRequest;
import com.smartshop.erp.dto.request.LigneAcompteRequest;
import com.smartshop.erp.dto.request.VersementAcompteRequest;
import com.smartshop.erp.dto.response.AcompteResponse;
import com.smartshop.erp.dto.response.LigneAcompteResponse;
import com.smartshop.erp.dto.response.VersementAcompteResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.StatutAcompte;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.AcompteService;
import com.smartshop.erp.util.GenerateurNumero;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AcompteServiceImpl implements AcompteService {

    private final AcompteRepository acompteRepository;
    private final VersementAcompteRepository versementAcompteRepository;
    private final ClientRepository clientRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProduitRepository produitRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityManager entityManager;

    @Override
    public List<AcompteResponse> lister(StatutAcompte statut, Long idBoutique) {
        List<Acompte> acomptes;
        if (statut != null) {
            acomptes = acompteRepository.findByStatut(statut);
        } else if (idBoutique != null) {
            acomptes = acompteRepository.findByBoutique_IdBoutique(idBoutique);
        } else {
            acomptes = acompteRepository.findAll();
        }
        if (idBoutique != null && statut != null) {
            acomptes = acomptes.stream().filter(a -> a.getBoutique().getIdBoutique().equals(idBoutique)).collect(Collectors.toList());
        }
        return acomptes.stream().map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    public AcompteResponse obtenir(Long idAcompte) {
        return versReponse(trouver(idAcompte));
    }

    @Override
    public List<AcompteResponse> parClient(Long idClient) {
        return acompteRepository.findByClient_IdClient(idClient).stream().map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AcompteResponse creer(AcompteRequest request, Long idVendeurConnecte) {
        Client client = clientRepository.findById(request.getIdClient())
                .orElseThrow(() -> new RessourceNonTrouveeException("Client introuvable, id=" + request.getIdClient()));

        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));

        Utilisateur vendeur = utilisateurRepository.findById(idVendeurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        Acompte acompte = Acompte.builder()
                .numeroAcompte(GenerateurNumero.generer("ACP"))
                .client(client)
                .boutique(boutique)
                .vendeur(vendeur)
                .observation(request.getObservation())
                .statut(StatutAcompte.EN_ATTENTE)
                .build();

        List<LigneAcompte> lignes = new ArrayList<>();
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneAcompteRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getIdProduit())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + ligneReq.getIdProduit()));

            BigDecimal prixUnitaire = ligneReq.getPrixUnitaire();
            if (prixUnitaire == null) {
                prixUnitaire = stockBoutiqueRepository
                        .findByProduit_IdProduitAndBoutique_IdBoutique(produit.getIdProduit(), boutique.getIdBoutique())
                        .map(sb -> sb.getPrixVente() != null ? sb.getPrixVente() : produit.getPrixCatalogue())
                        .orElse(produit.getPrixCatalogue());
            }

            BigDecimal sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
            montantTotal = montantTotal.add(sousTotal);

            lignes.add(LigneAcompte.builder()
                    .acompte(acompte)
                    .produit(produit)
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(prixUnitaire)
                    .sousTotal(sousTotal)
                    .build());
        }

        acompte.setLignes(lignes);
        acompte.setMontantTotal(montantTotal);
        acompte.setMontantVerse(BigDecimal.ZERO);
        acompte.setResteAPayer(montantTotal);

        acompte = acompteRepository.save(acompte);

        if (request.getVersementInitial() != null && request.getVersementInitial().compareTo(BigDecimal.ZERO) > 0) {
            appliquerVersement(acompte, request.getVersementInitial(), "Versement initial", vendeur);
        }

        return versReponse(acompte);
    }

    @Override
    @Transactional
    public AcompteResponse enregistrerVersement(Long idAcompte, VersementAcompteRequest request, Long idUtilisateurConnecte) {
        Acompte acompte = trouver(idAcompte);

        if (acompte.getStatut() != StatutAcompte.EN_ATTENTE) {
            throw new OperationInvalideException("Cet acompte n'est plus en attente de versement (statut=" + acompte.getStatut() + ")");
        }
        if (request.getMontant().compareTo(acompte.getResteAPayer()) > 0) {
            throw new OperationInvalideException("Le montant verse (" + request.getMontant()
                    + ") depasse le reste a payer (" + acompte.getResteAPayer() + ")");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        appliquerVersement(acompte, request.getMontant(), request.getObservation(), utilisateur);

        return versReponse(trouver(idAcompte));
    }

    @Override
    @Transactional
    public void annuler(Long idAcompte) {
        Acompte acompte = trouver(idAcompte);
        if (acompte.getStatut() == StatutAcompte.ANNULE) {
            throw new OperationInvalideException("Cet acompte est deja annule");
        }
        acompte.setStatut(StatutAcompte.ANNULE);
        acompteRepository.save(acompte);
    }

    // ---------------------------------------------------------------

    /**
     * Insere le versement ; le trigger SQL trg_acompte met a jour automatiquement
     * montant_verse, reste_a_payer et statut (SOLDE si reste_a_payer <= 0). Comme Hibernate
     * ignore les modifications faites par un trigger (elles ne passent pas par le contexte de
     * persistance), on force explicitement le rechargement de l'entite depuis la base ensuite.
     */
    private void appliquerVersement(Acompte acompte, BigDecimal montant, String observation, Utilisateur utilisateur) {
        versementAcompteRepository.save(VersementAcompte.builder()
                .acompte(acompte)
                .montant(montant)
                .observation(observation)
                .utilisateur(utilisateur)
                .build());
        entityManager.flush();
        entityManager.refresh(acompte);
    }

    private Acompte trouver(Long idAcompte) {
        return acompteRepository.findById(idAcompte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Acompte introuvable, id=" + idAcompte));
    }

    private AcompteResponse versReponse(Acompte acompte) {
        List<LigneAcompteResponse> lignes = acompte.getLignes().stream()
                .map(l -> LigneAcompteResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .build())
                .collect(Collectors.toList());

        List<VersementAcompteResponse> versements = versementAcompteRepository
                .findByAcompte_IdAcompteOrderByDateVersementDesc(acompte.getIdAcompte()).stream()
                .map(v -> VersementAcompteResponse.builder()
                        .idVersement(v.getIdVersement())
                        .montant(v.getMontant())
                        .dateVersement(v.getDateVersement())
                        .utilisateur(v.getUtilisateur() != null ? v.getUtilisateur().getNom() + " " + v.getUtilisateur().getPrenom() : null)
                        .observation(v.getObservation())
                        .build())
                .collect(Collectors.toList());

        // Note : dans les flux de creation/versement, l'appelant a deja rafraichi l'entite
        // (voir appliquerVersement) apres l'ecriture declenchee par les triggers SQL.
        return AcompteResponse.builder()
                .idAcompte(acompte.getIdAcompte())
                .numeroAcompte(acompte.getNumeroAcompte())
                .idClient(acompte.getClient().getIdClient())
                .client(acompte.getClient().getNom() + " " + (acompte.getClient().getPrenom() != null ? acompte.getClient().getPrenom() : ""))
                .telephoneClient(acompte.getClient().getTelephone())
                .idBoutique(acompte.getBoutique().getIdBoutique())
                .boutique(acompte.getBoutique().getNom())
                .vendeur(acompte.getVendeur().getNom() + " " + acompte.getVendeur().getPrenom())
                .montantTotal(acompte.getMontantTotal())
                .montantVerse(acompte.getMontantVerse())
                .resteAPayer(acompte.getResteAPayer())
                .statut(acompte.getStatut())
                .dateCreation(acompte.getDateCreation())
                .observation(acompte.getObservation())
                .lignes(lignes)
                .versements(versements)
                .build();
    }
}
