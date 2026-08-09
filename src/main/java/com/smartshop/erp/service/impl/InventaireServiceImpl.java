package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.InventaireRequest;
import com.smartshop.erp.dto.request.LigneInventaireRequest;
import com.smartshop.erp.dto.response.InventaireResponse;
import com.smartshop.erp.dto.response.LigneInventaireResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.TypeMouvement;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.InventaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventaireServiceImpl implements InventaireService {

    private final InventaireRepository inventaireRepository;
    private final LigneInventaireRepository ligneInventaireRepository;
    private final ProduitRepository produitRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<InventaireResponse> lister(Long idBoutique) {
        List<Inventaire> liste = idBoutique != null
                ? inventaireRepository.findAll().stream()
                    .filter(i -> i.getBoutique().getIdBoutique().equals(idBoutique))
                    .collect(Collectors.toList())
                : inventaireRepository.findAll();

        return liste.stream()
                .sorted((a, b) -> b.getDateInventaire().compareTo(a.getDateInventaire()))
                .map(this::versReponse)
                .collect(Collectors.toList());
    }

    @Override
    public InventaireResponse obtenir(Long idInventaire) {
        return versReponse(trouver(idInventaire));
    }

    @Override
    @Transactional
    public InventaireResponse creer(InventaireRequest request, Long idUtilisateurConnecte) {
        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        Inventaire inventaire = Inventaire.builder()
                .boutique(boutique)
                .utilisateur(utilisateur)
                .observation(request.getObservation())
                .build();
        inventaire = inventaireRepository.save(inventaire);

        for (LigneInventaireRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getIdProduit())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + ligneReq.getIdProduit()));

            StockBoutique stock = stockBoutiqueRepository
                    .findByProduit_IdProduitAndBoutique_IdBoutique(produit.getIdProduit(), boutique.getIdBoutique())
                    .orElseGet(() -> StockBoutique.builder().produit(produit).boutique(boutique).quantiteDisponible(0).build());

            int quantiteTheorique = stock.getQuantiteDisponible() == null ? 0 : stock.getQuantiteDisponible();
            int quantitePhysique = ligneReq.getQuantitePhysique();
            int ecart = quantitePhysique - quantiteTheorique;

            ligneInventaireRepository.save(LigneInventaire.builder()
                    .inventaire(inventaire)
                    .produit(produit)
                    .quantiteTheorique(quantiteTheorique)
                    .quantitePhysique(quantitePhysique)
                    .ecart(ecart)
                    .build());

            // Le stock de la boutique est aligne immediatement sur le comptage physique constate
            stock.setQuantiteDisponible(quantitePhysique);
            stockBoutiqueRepository.save(stock);

            if (ecart != 0) {
                mouvementStockRepository.save(MouvementStock.builder()
                        .produit(produit)
                        .typeMouvement(TypeMouvement.AJUSTEMENT_INVENTAIRE)
                        .quantite(ecart)
                        .boutiqueSource(ecart < 0 ? boutique : null)
                        .boutiqueDestination(ecart > 0 ? boutique : null)
                        .utilisateur(utilisateur)
                        .motif("Inventaire #" + inventaire.getIdInventaire())
                        .build());
            }
        }

        return versReponse(inventaire);
    }

    // ---------------------------------------------------------------

    private Inventaire trouver(Long idInventaire) {
        return inventaireRepository.findById(idInventaire)
                .orElseThrow(() -> new RessourceNonTrouveeException("Inventaire introuvable, id=" + idInventaire));
    }

    private InventaireResponse versReponse(Inventaire inventaire) {
        List<LigneInventaireResponse> lignes = ligneInventaireRepository
                .findByInventaire_IdInventaire(inventaire.getIdInventaire()).stream()
                .map(l -> LigneInventaireResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .quantiteTheorique(l.getQuantiteTheorique())
                        .quantitePhysique(l.getQuantitePhysique())
                        .ecart(l.getEcart())
                        .build())
                .collect(Collectors.toList());

        return InventaireResponse.builder()
                .idInventaire(inventaire.getIdInventaire())
                .idBoutique(inventaire.getBoutique().getIdBoutique())
                .boutique(inventaire.getBoutique().getNom())
                .utilisateur(inventaire.getUtilisateur().getNom() + " " + inventaire.getUtilisateur().getPrenom())
                .dateInventaire(inventaire.getDateInventaire())
                .observation(inventaire.getObservation())
                .lignes(lignes)
                .build();
    }
}
