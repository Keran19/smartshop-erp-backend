package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.LigneVenteRequest;
import com.smartshop.erp.dto.request.VenteRequest;
import com.smartshop.erp.dto.response.LigneVenteResponse;
import com.smartshop.erp.dto.response.VenteResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.enums.ModeReglement;
import com.smartshop.erp.enums.StatutVente;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.exception.StockInsuffisantException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.FacturePdfService;
import com.smartshop.erp.service.RapportVentesPdfService;
import com.smartshop.erp.service.VenteService;
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
public class VenteServiceImpl implements VenteService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final ProduitRepository produitRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final ClientRepository clientRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FactureRepository factureRepository;
    private final CreditRepository creditRepository;
    private final FacturePdfService facturePdfService;
    private final RapportVentesPdfService rapportVentesPdfService;

    @Override
    public VenteResponse apercu(VenteRequest request, Long idVendeurConnecte) {
        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));

        Client client = null;
        if (request.getIdClient() != null) {
            client = clientRepository.findById(request.getIdClient())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Client introuvable, id=" + request.getIdClient()));
        }

        Utilisateur vendeur = utilisateurRepository.findById(idVendeurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Vendeur introuvable"));

        List<LigneVenteResponse> lignesCalculees = new ArrayList<>();
        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneVenteRequest ligneReq : request.getLignes()) {
            Produit produit = produitRepository.findById(ligneReq.getIdProduit())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + ligneReq.getIdProduit()));

            BigDecimal prixUnitaire = determinerPrixUnitaire(produit, boutique.getIdBoutique(), ligneReq.getPrixUnitaire());
            BigDecimal prixAchat = produit.getPrixAchat() == null ? BigDecimal.ZERO : produit.getPrixAchat();
            BigDecimal sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(ligneReq.getQuantite()));

            montantTotal = montantTotal.add(sousTotal);

            lignesCalculees.add(LigneVenteResponse.builder()
                    .idProduit(produit.getIdProduit())
                    .nomProduit(produit.getNom())
                    .codeBarres(produit.getCodeBarres())
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(prixUnitaire)
                    .sousTotal(sousTotal)
                    .benefice(prixUnitaire.subtract(prixAchat).multiply(BigDecimal.valueOf(ligneReq.getQuantite())))
                    .build());
        }

        BigDecimal remise = request.getRemiseGlobale() == null ? BigDecimal.ZERO : request.getRemiseGlobale();
        BigDecimal montantFinal = montantTotal.subtract(remise);
        BigDecimal montantRecu = request.getMontantRecu();
        BigDecimal monnaieRendue = BigDecimal.ZERO;
        if (montantRecu != null) {
            BigDecimal diff = montantRecu.subtract(montantFinal);
            monnaieRendue = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
        }

        BigDecimal beneficeTotal = lignesCalculees.stream()
                .map(LigneVenteResponse::getBenefice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VenteResponse.builder()
                .idVente(null)
                .numeroVente(null)
                .dateVente(LocalDateTime.now())
                .idBoutique(boutique.getIdBoutique())
                .boutique(boutique.getNom())
                .idClient(client != null ? client.getIdClient() : null)
                .client(client != null ? (client.getNom() + " " + (client.getPrenom() != null ? client.getPrenom() : "")).trim() : null)
                .idVendeur(vendeur.getIdUtilisateur())
                .vendeur(vendeur.getNom() + " " + vendeur.getPrenom())
                .montantTotal(montantTotal)
                .remiseGlobale(remise)
                .montantFinal(montantFinal)
                .montantRecu(montantRecu)
                .monnaieRendue(monnaieRendue)
                .modeReglement(request.getModeReglement())
                .statut(StatutVente.EN_ATTENTE)
                .observation(request.getObservation())
                .benefice(beneficeTotal)
                .lignes(lignesCalculees)
                .numeroFacture(null)
                .facturedImprimee(false)
                .build();
    }

    @Override
    @Transactional
    public VenteResponse valider(VenteRequest request, Long idVendeurConnecte) {
        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));

        Client client = null;
        if (request.getIdClient() != null) {
            client = clientRepository.findById(request.getIdClient())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Client introuvable, id=" + request.getIdClient()));
        }
        if (request.getModeReglement() == ModeReglement.CREDIT && client == null) {
            throw new OperationInvalideException("Un client est obligatoire pour une vente a credit");
        }

        Utilisateur vendeur = utilisateurRepository.findById(idVendeurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Vendeur introuvable"));

        // 1) Verification du stock disponible pour chaque ligne AVANT toute ecriture
        List<StockBoutique> stocksConcernes = new ArrayList<>();
        for (LigneVenteRequest ligneReq : request.getLignes()) {
            StockBoutique stock = stockBoutiqueRepository
                    .findByProduit_IdProduitAndBoutique_IdBoutique(ligneReq.getIdProduit(), request.getIdBoutique())
                    .orElseThrow(() -> new StockInsuffisantException(
                            "Ce produit n'est pas reference dans le stock de cette boutique (id_produit=" + ligneReq.getIdProduit() + ")"));

            if (stock.getQuantiteDisponible() == null || stock.getQuantiteDisponible() < ligneReq.getQuantite()) {
                throw new StockInsuffisantException("Stock insuffisant pour \"" + stock.getProduit().getNom()
                        + "\" : disponible=" + stock.getQuantiteDisponible() + ", demande=" + ligneReq.getQuantite());
            }
            stocksConcernes.add(stock);
        }

        // 2) Creation de l'entete de vente
        Vente vente = Vente.builder()
                .numeroVente(GenerateurNumero.generer("VEN"))
                .boutique(boutique)
                .client(client)
                .vendeur(vendeur)
                .modeReglement(request.getModeReglement())
                .statut(StatutVente.VALIDEE)
                .remiseGlobale(request.getRemiseGlobale() == null ? BigDecimal.ZERO : request.getRemiseGlobale())
                .observation(request.getObservation())
                .build();

        BigDecimal montantTotal = BigDecimal.ZERO;
        List<LigneVente> lignes = new ArrayList<>();

        for (int i = 0; i < request.getLignes().size(); i++) {
            LigneVenteRequest ligneReq = request.getLignes().get(i);
            StockBoutique stock = stocksConcernes.get(i);
            Produit produit = stock.getProduit();

            BigDecimal prixUnitaire = determinerPrixUnitaire(produit, boutique.getIdBoutique(), ligneReq.getPrixUnitaire());
            BigDecimal prixAchat = produit.getPrixAchat() == null ? BigDecimal.ZERO : produit.getPrixAchat();
            BigDecimal sousTotal = prixUnitaire.multiply(BigDecimal.valueOf(ligneReq.getQuantite()));
            montantTotal = montantTotal.add(sousTotal);

            LigneVente ligne = LigneVente.builder()
                    .vente(vente)
                    .produit(produit)
                    .quantite(ligneReq.getQuantite())
                    .prixUnitaire(prixUnitaire)
                    .prixAchatUnitaire(prixAchat)
                    .sousTotal(sousTotal)
                    .build();
            lignes.add(ligne);
        }

        vente.setMontantTotal(montantTotal);
        BigDecimal montantFinal = montantTotal.subtract(vente.getRemiseGlobale());
        vente.setMontantFinal(montantFinal);
        vente.setMontantRecu(request.getMontantRecu());

        BigDecimal monnaieRendue = BigDecimal.ZERO;
        if (request.getMontantRecu() != null) {
            BigDecimal diff = request.getMontantRecu().subtract(montantFinal);
            monnaieRendue = diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
        }
        vente.setMonnaieRendue(monnaieRendue);
        vente.setLignes(lignes);

        // La sauvegarde de "ligne_vente" declenche le trigger SQL trg_sortie_stock qui deduit
        // automatiquement stock_boutique.quantite_disponible et journalise le mouvement de stock.
        vente = venteRepository.save(vente);

        // 3) Facture liee (pas encore imprimee)
        Facture facture = Facture.builder()
                .numeroFacture(GenerateurNumero.generer("FAC"))
                .vente(vente)
                .imprimee(false)
                .build();
        factureRepository.save(facture);

        // 4) Si vente a credit -> creation de l'enregistrement credit
        if (request.getModeReglement() == ModeReglement.CREDIT) {
            BigDecimal montantPaye = request.getMontantRecu() == null ? BigDecimal.ZERO : request.getMontantRecu();
            if (montantPaye.compareTo(montantFinal) > 0) montantPaye = montantFinal;

            Credit credit = Credit.builder()
                    .vente(vente)
                    .client(client)
                    .montantInitial(montantFinal)
                    .montantPaye(montantPaye)
                    .resteAPayer(montantFinal.subtract(montantPaye))
                    .dateLimite(request.getDateLimiteCredit())
                    .build();
            creditRepository.save(credit);
        }

        return construireReponse(vente, facture);
    }

    @Override
    public VenteResponse obtenir(Long idVente) {
        Vente vente = venteRepository.findById(idVente)
                .orElseThrow(() -> new RessourceNonTrouveeException("Vente introuvable, id=" + idVente));
        Facture facture = factureRepository.findByVente_IdVente(idVente).orElse(null);
        return construireReponse(vente, facture);
    }

    @Override
    public List<VenteResponse> historique(LocalDateTime debut, LocalDateTime fin, Long idBoutique) {
        return venteRepository.findByPeriode(debut, fin, idBoutique).stream()
                .map(v -> construireReponse(v, factureRepository.findByVente_IdVente(v.getIdVente()).orElse(null)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void annuler(Long idVente) {
        Vente vente = venteRepository.findById(idVente)
                .orElseThrow(() -> new RessourceNonTrouveeException("Vente introuvable, id=" + idVente));
        if (vente.getStatut() == StatutVente.ANNULEE) {
            throw new OperationInvalideException("Cette vente est deja annulee");
        }
        // NB : par choix, l'annulation ne remet pas automatiquement le stock a jour ;
        // utiliser une entree de mouvement de stock (AJUSTEMENT_INVENTAIRE) si une remise en stock est necessaire.
        vente.setStatut(StatutVente.ANNULEE);
        venteRepository.save(vente);
    }

    @Override
    @Transactional // ecrit facture.imprimee : ne doit pas heriter du readOnly=true de la classe
    public String imprimer(Long idVente) {
        Vente vente = venteRepository.findById(idVente)
                .orElseThrow(() -> new RessourceNonTrouveeException("Vente introuvable, id=" + idVente));
        Facture facture = factureRepository.findByVente_IdVente(idVente)
                .orElseThrow(() -> new RessourceNonTrouveeException("Facture introuvable pour la vente id=" + idVente));

        String chemin = facturePdfService.genererPdfVente(vente, facture);

        facture.setImprimee(true);
        facture.setDateImpression(LocalDateTime.now());
        factureRepository.save(facture);

        return chemin;
    }

    @Override
    @Transactional(readOnly = true)
    public String genererRapportPdf(LocalDateTime debut, LocalDateTime fin, Long idBoutique) {
        List<Vente> ventes = venteRepository.findByPeriode(debut, fin, idBoutique);
        // Charge explicitement les lignes de chaque vente pendant que la session Hibernate est
        // encore ouverte (necessaire a Vente.getBenefice(), qui parcourt la collection lazy "lignes").
        ventes.forEach(v -> v.getLignes().size());
        return rapportVentesPdfService.genererPdfListeVentes(ventes, debut, fin, idBoutique);
    }

    // ---------------------------------------------------------------

    private BigDecimal determinerPrixUnitaire(Produit produit, Long idBoutique, BigDecimal prixForce) {
        if (prixForce != null) return prixForce;
        return stockBoutiqueRepository.findByProduit_IdProduitAndBoutique_IdBoutique(produit.getIdProduit(), idBoutique)
                .map(sb -> sb.getPrixVente() != null ? sb.getPrixVente() : produit.getPrixCatalogue())
                .orElse(produit.getPrixCatalogue());
    }

    private VenteResponse construireReponse(Vente vente, Facture facture) {
        List<LigneVente> lignes = ligneVenteRepository.findByVente_IdVente(vente.getIdVente());
        // Si l'entite vient d'etre sauvee, vente.getLignes() est deja peuplee ; sinon on recharge.
        List<LigneVente> lignesEffectives = lignes.isEmpty() ? vente.getLignes() : lignes;

        List<LigneVenteResponse> lignesResponse = lignesEffectives.stream()
                .map(l -> LigneVenteResponse.builder()
                        .idProduit(l.getProduit().getIdProduit())
                        .nomProduit(l.getProduit().getNom())
                        .codeBarres(l.getProduit().getCodeBarres())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .benefice(l.getBenefice())
                        .build())
                .collect(Collectors.toList());

        BigDecimal beneficeTotal = lignesResponse.stream()
                .map(LigneVenteResponse::getBenefice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VenteResponse.builder()
                .idVente(vente.getIdVente())
                .numeroVente(vente.getNumeroVente())
                .dateVente(vente.getDateVente())
                .idBoutique(vente.getBoutique().getIdBoutique())
                .boutique(vente.getBoutique().getNom())
                .idClient(vente.getClient() != null ? vente.getClient().getIdClient() : null)
                .client(vente.getClient() != null
                        ? (vente.getClient().getNom() + " " + (vente.getClient().getPrenom() != null ? vente.getClient().getPrenom() : "")).trim()
                        : null)
                .idVendeur(vente.getVendeur().getIdUtilisateur())
                .vendeur(vente.getVendeur().getNom() + " " + vente.getVendeur().getPrenom())
                .montantTotal(vente.getMontantTotal())
                .remiseGlobale(vente.getRemiseGlobale())
                .montantFinal(vente.getMontantFinal())
                .montantRecu(vente.getMontantRecu())
                .monnaieRendue(vente.getMonnaieRendue())
                .modeReglement(vente.getModeReglement())
                .statut(vente.getStatut())
                .observation(vente.getObservation())
                .benefice(beneficeTotal)
                .lignes(lignesResponse)
                .numeroFacture(facture != null ? facture.getNumeroFacture() : null)
                .facturedImprimee(facture != null ? facture.getImprimee() : false)
                .build();
    }
}
