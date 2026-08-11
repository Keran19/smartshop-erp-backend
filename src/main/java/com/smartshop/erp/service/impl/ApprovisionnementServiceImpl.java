package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.ApprovisionnementRequest;
import com.smartshop.erp.dto.request.LigneApprovisionnementRequest;
import com.smartshop.erp.dto.response.ApprovisionnementResponse;
import com.smartshop.erp.dto.response.LigneApprovisionnementResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.StatutApprovisionnement;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.ApprovisionnementService;
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
public class ApprovisionnementServiceImpl implements ApprovisionnementService {

    private final ApprovisionnementRepository approvisionnementRepository;
    private final HistoriqueApprovisionnementRepository historiqueApprovisionnementRepository;
    private final FournisseurRepository fournisseurRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityManager entityManager;

    @Override
    public List<ApprovisionnementResponse> lister(Long idBoutique) {
        List<Approvisionnement> liste = approvisionnementRepository.findAll();
        return liste.stream()
                .filter(a -> idBoutique == null || a.getBoutique().getIdBoutique().equals(idBoutique))
                .sorted((a, b) -> b.getDateCreation().compareTo(a.getDateCreation()))
                .map(this::versReponse)
                .collect(Collectors.toList());
    }

    @Override
    public ApprovisionnementResponse obtenir(Long id) {
        return versReponse(trouver(id));
    }

    @Override
    @Transactional
    public ApprovisionnementResponse creer(ApprovisionnementRequest request, Long idGerantConnecte) {
        Fournisseur fournisseur = fournisseurRepository.findById(request.getIdFournisseur())
                .orElseThrow(() -> new RessourceNonTrouveeException("Fournisseur introuvable, id=" + request.getIdFournisseur()));
        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));
        Utilisateur gerant = utilisateurRepository.findById(idGerantConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        Approvisionnement appro = Approvisionnement.builder()
                .numeroApprovisionnement(GenerateurNumero.generer("APP"))
                .fournisseur(fournisseur)
                .boutique(boutique)
                .gerant(gerant)
                .statut(StatutApprovisionnement.RECU) // reception immediate : le stock est incremente tout de suite
                .observation(request.getObservation())
                .build();

        BigDecimal montantTotal = BigDecimal.ZERO;
        List<LigneApprovisionnement> lignes = new ArrayList<>();

        for (LigneApprovisionnementRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getIdProduit())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + ligneReq.getIdProduit()));

            BigDecimal sousTotal = ligneReq.getPrixAchat().multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
            montantTotal = montantTotal.add(sousTotal);

            lignes.add(LigneApprovisionnement.builder()
                    .approvisionnement(appro)
                    .produit(produit)
                    .quantite(ligneReq.getQuantite())
                    .prixAchat(ligneReq.getPrixAchat())
                    .sousTotal(sousTotal)
                    .build());

            // Le cout d'achat "courant" du produit reste mis a jour pour compatibilite avec le
            // reste de l'appli (rapports, marge par defaut), mais le suivi FIFO ci-dessous
            // (historique_approvisionnement.quantite_restante) est desormais la source de verite
            // pour savoir a quel prix est realmente vendu le stock au fil de son ecoulement.
            produit.setPrixAchat(ligneReq.getPrixAchat());
            produitRepository.save(produit);
        }

        appro.setLignes(lignes);
        appro.setMontantTotal(montantTotal);

        // La sauvegarde des lignes declenche le trigger SQL trg_entree_stock qui incremente
        // automatiquement stock_boutique.quantite_disponible et journalise le mouvement de stock.
        appro = approvisionnementRepository.save(appro);
        entityManager.flush();

        // Historique d'achat par produit/fournisseur (traçabilite) + nouveau lot FIFO plein
        // (quantite_restante = quantite recue : rien n'en a encore ete vendu).
        for (LigneApprovisionnement ligne : appro.getLignes()) {
            historiqueApprovisionnementRepository.save(HistoriqueApprovisionnement.builder()
                    .produit(ligne.getProduit())
                    .fournisseur(fournisseur)
                    .approvisionnement(appro)
                    .quantite(ligne.getQuantite())
                    .quantiteRestante(ligne.getQuantite())
                    .prixAchat(ligne.getPrixAchat())
                    .build());
        }

        return versReponse(appro);
    }

    // ---------------------------------------------------------------

    private Approvisionnement trouver(Long id) {
        return approvisionnementRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Approvisionnement introuvable, id=" + id));
    }

    private ApprovisionnementResponse versReponse(Approvisionnement appro) {
        List<LigneApprovisionnementResponse> lignes = appro.getLignes().stream()
                .map(l -> LigneApprovisionnementResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .quantite(l.getQuantite())
                        .prixAchat(l.getPrixAchat())
                        .sousTotal(l.getSousTotal())
                        .build())
                .collect(Collectors.toList());

        return ApprovisionnementResponse.builder()
                .idApprovisionnement(appro.getIdApprovisionnement())
                .numeroApprovisionnement(appro.getNumeroApprovisionnement())
                .dateCreation(appro.getDateCreation())
                .fournisseur(appro.getFournisseur().getNom())
                .idBoutique(appro.getBoutique().getIdBoutique())
                .boutique(appro.getBoutique().getNom())
                .gerant(appro.getGerant().getNom() + " " + appro.getGerant().getPrenom())
                .montantTotal(appro.getMontantTotal())
                .statut(appro.getStatut())
                .observation(appro.getObservation())
                .lignes(lignes)
                .build();
    }
}
