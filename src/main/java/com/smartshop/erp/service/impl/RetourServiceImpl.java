package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.LigneEchangeRequest;
import com.smartshop.erp.dto.request.LigneRetourRequest;
import com.smartshop.erp.dto.request.RetourRequest;
import com.smartshop.erp.dto.response.LigneEchangeResponse;
import com.smartshop.erp.dto.response.LigneRetourResponse;
import com.smartshop.erp.dto.response.RetourResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.StatutRetour;
import com.smartshop.erp.enums.StatutVente;
import com.smartshop.erp.enums.TypeMouvement;
import com.smartshop.erp.enums.TypeRetour;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.exception.StockInsuffisantException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.RetourService;
import com.smartshop.erp.util.GenerateurNumero;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // securise les acces aux associations lazy (boutique, client, categorie...) hors des methodes d'ecriture, qui restent annotees @Transactional individuellement
public class RetourServiceImpl implements RetourService {

    private static final BigDecimal SEUIL_EGALITE = new BigDecimal("0.01");

    private final RetourRepository retourRepository;
    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final ProduitRepository produitRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public RetourResponse creer(RetourRequest request, Long idUtilisateurConnecte) {

        Vente vente = venteRepository.findById(request.getIdVente())
                .orElseThrow(() -> new RessourceNonTrouveeException("Vente introuvable, id=" + request.getIdVente()));

        if (vente.getStatut() != StatutVente.VALIDEE) {
            throw new OperationInvalideException("Seule une vente validee peut faire l'objet d'un retour");
        }

        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        boolean estEchange = request.getTypeRetour() != TypeRetour.REMBOURSEMENT;

        if (!estEchange && request.getLignesEchange() != null && !request.getLignesEchange().isEmpty()) {
            throw new OperationInvalideException("Un remboursement ne doit pas contenir de lignes d'echange");
        }
        if (estEchange && (request.getLignesEchange() == null || request.getLignesEchange().isEmpty())) {
            throw new OperationInvalideException("Un echange necessite au moins un produit donne en retour");
        }

        Retour retour = Retour.builder()
                .numeroRetour(GenerateurNumero.generer("RET"))
                .vente(vente)
                .boutique(vente.getBoutique())
                .utilisateur(utilisateur)
                .typeRetour(request.getTypeRetour())
                .observation(request.getObservation())
                .statut(StatutRetour.VALIDE)
                .build();

        // 1) Traitement des produits rendus par le client
        List<LigneRetour> lignesRetour = new ArrayList<>();
        BigDecimal montantRetourne = BigDecimal.ZERO;

        for (LigneRetourRequest ligneReq : request.getLignesRetour()) {
            LigneVente ligneVenteOrigine = ligneVenteRepository
                    .findByVente_IdVenteAndProduit_IdProduit(vente.getIdVente(), ligneReq.getIdProduit())
                    .orElseThrow(() -> new OperationInvalideException(
                            "Le produit id=" + ligneReq.getIdProduit() + " ne fait pas partie de la vente d'origine"));

            Integer dejaRetourne = retourRepository.quantiteDejaRetournee(vente.getIdVente(), ligneReq.getIdProduit());
            int quantiteDisponibleAuRetour = ligneVenteOrigine.getQuantite() - (dejaRetourne == null ? 0 : dejaRetourne);

            if (ligneReq.getQuantite() > quantiteDisponibleAuRetour) {
                throw new OperationInvalideException("Quantite retournee (" + ligneReq.getQuantite()
                        + ") superieure a la quantite encore retournable pour \"" + ligneVenteOrigine.getProduit().getNom()
                        + "\" (restant : " + quantiteDisponibleAuRetour + ")");
            }

            BigDecimal sousTotal = ligneVenteOrigine.getPrixUnitaire().multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
            montantRetourne = montantRetourne.add(sousTotal);

            LigneRetour ligne = LigneRetour.builder()
                    .retour(retour)
                    .produit(ligneVenteOrigine.getProduit())
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(ligneVenteOrigine.getPrixUnitaire())
                    .sousTotal(sousTotal)
                    .motif(ligneReq.getMotif())
                    .build();
            lignesRetour.add(ligne);

            // Le produit rendu reintegre le stock de la boutique de vente
            remettreEnStock(ligneVenteOrigine.getProduit(), vente.getBoutique(), ligneReq.getQuantite(), utilisateur,
                    "Retour client - vente #" + vente.getNumeroVente());
        }
        retour.setLignesRetour(lignesRetour);
        retour.setMontantRetourne(montantRetourne);

        // 2) Traitement des produits donnes en echange (le cas echeant)
        List<LigneEchange> lignesEchange = new ArrayList<>();
        BigDecimal montantEchange = BigDecimal.ZERO;

        if (estEchange) {
            for (LigneEchangeRequest ligneReq : request.getLignesEchange()) {
                Produit produit = produitRepository.findById(ligneReq.getIdProduit())
                        .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + ligneReq.getIdProduit()));

                StockBoutique stock = stockBoutiqueRepository
                        .findByProduit_IdProduitAndBoutique_IdBoutique(produit.getIdProduit(), vente.getBoutique().getIdBoutique())
                        .orElseThrow(() -> new StockInsuffisantException(
                                "Produit non reference dans le stock de cette boutique : " + produit.getNom()));

                if (stock.getQuantiteDisponible() == null || stock.getQuantiteDisponible() < ligneReq.getQuantite()) {
                    throw new StockInsuffisantException("Stock insuffisant pour \"" + produit.getNom()
                            + "\" : disponible=" + stock.getQuantiteDisponible() + ", demande=" + ligneReq.getQuantite());
                }

                BigDecimal prixUnitaire = stock.getPrixVente() != null ? stock.getPrixVente() : produit.getPrixCatalogue();
                BigDecimal sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
                montantEchange = montantEchange.add(sousTotal);

                LigneEchange ligne = LigneEchange.builder()
                        .retour(retour)
                        .produit(produit)
                        .quantite(ligneReq.getQuantite())
                        .prixUnitaire(prixUnitaire)
                        .sousTotal(sousTotal)
                        .build();
                lignesEchange.add(ligne);

                // Le produit donne en echange sort du stock
                retirerDuStock(stock, ligneReq.getQuantite(), utilisateur, "Echange - vente #" + vente.getNumeroVente());
            }
        }
        retour.setLignesEchange(lignesEchange);
        retour.setMontantEchange(montantEchange);

        // 3) Calcul financier selon le type de retour
        BigDecimal difference = montantEchange.subtract(montantRetourne);

        switch (request.getTypeRetour()) {
            case REMBOURSEMENT -> {
                retour.setMontantRembourse(montantRetourne);
                retour.setMontantComplement(BigDecimal.ZERO);
            }
            case ECHANGE_MEME_VALEUR -> {
                if (difference.abs().compareTo(SEUIL_EGALITE) > 0) {
                    throw new OperationInvalideException(
                            "Les produits echanges n'ont pas la meme valeur (retourne=" + montantRetourne
                            + ", echange=" + montantEchange + "). Utilisez ECHANGE_VALEUR_DIFFERENTE.");
                }
                retour.setMontantRembourse(BigDecimal.ZERO);
                retour.setMontantComplement(BigDecimal.ZERO);
            }
            case ECHANGE_VALEUR_DIFFERENTE -> {
                if (difference.compareTo(BigDecimal.ZERO) > 0) {
                    // les nouveaux produits valent plus cher -> le client paie la difference
                    retour.setMontantComplement(difference);
                    retour.setMontantRembourse(BigDecimal.ZERO);
                } else {
                    // les nouveaux produits valent moins cher -> on rembourse la difference
                    retour.setMontantRembourse(difference.abs());
                    retour.setMontantComplement(BigDecimal.ZERO);
                }
            }
        }

        retour = retourRepository.save(retour);
        return construireReponse(retour);
    }

    @Override
    public RetourResponse obtenir(Long idRetour) {
        return construireReponse(trouver(idRetour));
    }

    @Override
    public List<RetourResponse> parVente(Long idVente) {
        return retourRepository.findByVente_IdVente(idVente).stream()
                .map(this::construireReponse).collect(Collectors.toList());
    }

    @Override
    public List<RetourResponse> historique(LocalDateTime debut, LocalDateTime fin, Long idBoutique) {
        return retourRepository.findByPeriode(debut, fin, idBoutique).stream()
                .map(this::construireReponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void annuler(Long idRetour) {
        Retour retour = trouver(idRetour);
        if (retour.getStatut() == StatutRetour.ANNULE) {
            throw new OperationInvalideException("Ce retour est deja annule");
        }
        // NB : par choix, l'annulation d'un retour ne desfait pas automatiquement les mouvements
        // de stock deja effectues ; utiliser un mouvement d'AJUSTEMENT_INVENTAIRE si necessaire.
        retour.setStatut(StatutRetour.ANNULE);
        retourRepository.save(retour);
    }

    // ---------------------------------------------------------------

    private void remettreEnStock(Produit produit, Boutique boutique, int quantite, Utilisateur utilisateur, String motif) {
        StockBoutique stock = stockBoutiqueRepository
                .findByProduit_IdProduitAndBoutique_IdBoutique(produit.getIdProduit(), boutique.getIdBoutique())
                .orElseGet(() -> StockBoutique.builder()
                        .produit(produit).boutique(boutique).quantiteDisponible(0).build());

        stock.setQuantiteDisponible((stock.getQuantiteDisponible() == null ? 0 : stock.getQuantiteDisponible()) + quantite);
        stockBoutiqueRepository.save(stock);

        mouvementStockRepository.save(MouvementStock.builder()
                .produit(produit)
                .typeMouvement(TypeMouvement.RETOUR_CLIENT)
                .quantite(quantite)
                .boutiqueDestination(boutique)
                .utilisateur(utilisateur)
                .motif(motif)
                .build());
    }

    private void retirerDuStock(StockBoutique stock, int quantite, Utilisateur utilisateur, String motif) {
        stock.setQuantiteDisponible(stock.getQuantiteDisponible() - quantite);
        stockBoutiqueRepository.save(stock);

        mouvementStockRepository.save(MouvementStock.builder()
                .produit(stock.getProduit())
                .typeMouvement(TypeMouvement.SORTIE_ECHANGE)
                .quantite(quantite)
                .boutiqueSource(stock.getBoutique())
                .utilisateur(utilisateur)
                .motif(motif)
                .build());
    }

    private Retour trouver(Long idRetour) {
        return retourRepository.findById(idRetour)
                .orElseThrow(() -> new RessourceNonTrouveeException("Retour introuvable, id=" + idRetour));
    }

    private RetourResponse construireReponse(Retour retour) {
        List<LigneRetourResponse> lignesRetour = retour.getLignesRetour().stream()
                .map(l -> LigneRetourResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .motif(l.getMotif())
                        .build())
                .collect(Collectors.toList());

        List<LigneEchangeResponse> lignesEchange = retour.getLignesEchange().stream()
                .map(l -> LigneEchangeResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .build())
                .collect(Collectors.toList());

        return RetourResponse.builder()
                .idRetour(retour.getIdRetour())
                .numeroRetour(retour.getNumeroRetour())
                .idVente(retour.getVente().getIdVente())
                .numeroVenteOrigine(retour.getVente().getNumeroVente())
                .idBoutique(retour.getBoutique().getIdBoutique())
                .boutique(retour.getBoutique().getNom())
                .idUtilisateur(retour.getUtilisateur().getIdUtilisateur())
                .utilisateur(retour.getUtilisateur().getNom() + " " + retour.getUtilisateur().getPrenom())
                .typeRetour(retour.getTypeRetour())
                .statut(retour.getStatut())
                .dateRetour(retour.getDateRetour())
                .observation(retour.getObservation())
                .montantRetourne(retour.getMontantRetourne())
                .montantEchange(retour.getMontantEchange())
                .montantRembourse(retour.getMontantRembourse())
                .montantComplement(retour.getMontantComplement())
                .lignesRetour(lignesRetour)
                .lignesEchange(lignesEchange)
                .build();
    }
}
