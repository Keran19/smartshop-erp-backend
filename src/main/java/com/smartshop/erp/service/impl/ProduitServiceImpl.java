package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.ProduitRequest;
import com.smartshop.erp.dto.response.HistoriqueVenteProduitResponse;
import com.smartshop.erp.dto.response.ProduitResponse;
import com.smartshop.erp.dto.response.StockBoutiqueResponse;
import com.smartshop.erp.dto.response.VenteLigneHistoriqueResponse;
import com.smartshop.erp.entity.*;
import com.smartshop.erp.exception.ProduitIntrouvableParCodeBarresException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.*;
import com.smartshop.erp.service.ProduitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // securise les acces aux associations lazy (boutique, client, categorie...) hors des methodes d'ecriture, qui restent annotees @Transactional individuellement
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final CategorieRepository categorieRepository;
    private final MarqueRepository marqueRepository;
    private final FournisseurRepository fournisseurRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final LigneVenteRepository ligneVenteRepository;

    @Override
    public List<ProduitResponse> listerTous(Long idBoutique) {
        return produitRepository.findAll().stream()
                .map(p -> versReponse(p, idBoutique))
                .collect(Collectors.toList());
    }

    @Override
    public ProduitResponse obtenirParId(Long idProduit) {
        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + idProduit));
        return versReponse(produit, null);
    }

    @Override
    public ProduitResponse obtenirParCodeBarres(String codeBarres) {
        Produit produit = produitRepository.findByCodeBarres(codeBarres)
                .orElseThrow(() -> new ProduitIntrouvableParCodeBarresException(codeBarres));
        return versReponse(produit, null);
    }

    @Override
    public List<ProduitResponse> rechercher(String motCle) {
        return produitRepository
                .findByNomContainingIgnoreCaseOrReferenceContainingIgnoreCaseOrCodeBarresContainingIgnoreCase(motCle, motCle, motCle)
                .stream().map(p -> versReponse(p, null)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProduitResponse creer(ProduitRequest request) {
        if (produitRepository.existsByCodeBarres(request.getCodeBarres())) {
            throw new IllegalArgumentException("Un produit avec ce code-barres existe deja : " + request.getCodeBarres());
        }

        Categorie categorie = categorieRepository.findById(request.getIdCategorie())
                .orElseThrow(() -> new RessourceNonTrouveeException("Categorie introuvable, id=" + request.getIdCategorie()));

        Marque marque = null;
        if (request.getIdMarque() != null) {
            marque = marqueRepository.findById(request.getIdMarque())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Marque introuvable, id=" + request.getIdMarque()));
        }

        Fournisseur fournisseur = null;
        if (request.getIdFournisseur() != null) {
            fournisseur = fournisseurRepository.findById(request.getIdFournisseur())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Fournisseur introuvable, id=" + request.getIdFournisseur()));
        }

        Produit produit = Produit.builder()
                .codeBarres(request.getCodeBarres())
                .reference(request.getReference())
                .nom(request.getNom())
                .description(request.getDescription())
                .prixAchat(request.getPrixAchat())
                .prixCatalogue(request.getPrixVente())
                .seuilAlerte(request.getSeuilAlerte() == null ? 0 : request.getSeuilAlerte())
                .image(request.getImage())
                .poidsG(request.getPoidsG())
                .volumeMl(request.getVolumeMl())
                .categorie(categorie)
                .marque(marque)
                .fournisseur(fournisseur)
                .actif(true)
                .build();

        produit = produitRepository.save(produit);

        // Stock initial optionnel dans une boutique donnee
        if (request.getIdBoutique() != null) {
            Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));
            StockBoutique stock = StockBoutique.builder()
                    .produit(produit)
                    .boutique(boutique)
                    .quantiteDisponible(request.getQuantiteInitiale() == null ? 0 : request.getQuantiteInitiale())
                    .prixVente(request.getPrixVente())
                    .build();
            stockBoutiqueRepository.save(stock);
        }

        return versReponse(produit, request.getIdBoutique());
    }

    @Override
    @Transactional
    public ProduitResponse modifier(Long idProduit, ProduitRequest request) {
        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + idProduit));

        if (!produit.getCodeBarres().equals(request.getCodeBarres())
                && produitRepository.existsByCodeBarres(request.getCodeBarres())) {
            throw new IllegalArgumentException("Un produit avec ce code-barres existe deja : " + request.getCodeBarres());
        }

        Categorie categorie = categorieRepository.findById(request.getIdCategorie())
                .orElseThrow(() -> new RessourceNonTrouveeException("Categorie introuvable, id=" + request.getIdCategorie()));

        produit.setCodeBarres(request.getCodeBarres());
        produit.setReference(request.getReference());
        produit.setNom(request.getNom());
        produit.setDescription(request.getDescription());
        produit.setPrixAchat(request.getPrixAchat());
        produit.setPrixCatalogue(request.getPrixVente()); // le trigger trg_historique_prix journalise le changement
        produit.setSeuilAlerte(request.getSeuilAlerte());
        produit.setPoidsG(request.getPoidsG());
        produit.setVolumeMl(request.getVolumeMl());
        produit.setCategorie(categorie);

        if (request.getIdMarque() != null) {
            produit.setMarque(marqueRepository.findById(request.getIdMarque())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Marque introuvable")));
        }
        if (request.getIdFournisseur() != null) {
            produit.setFournisseur(fournisseurRepository.findById(request.getIdFournisseur())
                    .orElseThrow(() -> new RessourceNonTrouveeException("Fournisseur introuvable")));
        }

        produit = produitRepository.save(produit);
        return versReponse(produit, null);
    }

    @Override
    @Transactional
    public void desactiver(Long idProduit) {
        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + idProduit));
        produit.setActif(false);
        produitRepository.save(produit);
    }

    @Override
    public List<ProduitResponse> produitsEnAlerte(Long idBoutique) {
        List<StockBoutique> stocks = idBoutique != null
                ? stockBoutiqueRepository.findByBoutique_IdBoutique(idBoutique)
                : stockBoutiqueRepository.findAll();

        return stocks.stream()
                .filter(sb -> {
                    int seuil = sb.getSeuilAlerte() != null ? sb.getSeuilAlerte() : sb.getProduit().getSeuilAlerte();
                    return sb.getQuantiteDisponible() != null && sb.getQuantiteDisponible() <= seuil;
                })
                .map(sb -> versReponse(sb.getProduit(), sb.getBoutique().getIdBoutique()))
                .collect(Collectors.toList());
    }

    @Override
    public HistoriqueVenteProduitResponse historiqueVentes(String codeBarres, LocalDateTime debut, LocalDateTime fin) {
        Produit produit = produitRepository.findByCodeBarres(codeBarres)
                .orElseThrow(() -> new ProduitIntrouvableParCodeBarresException(codeBarres));

        List<LigneVente> lignes = ligneVenteRepository.historiqueParCodeBarres(codeBarres, debut, fin);

        List<VenteLigneHistoriqueResponse> ventes = lignes.stream()
                .map(l -> VenteLigneHistoriqueResponse.builder()
                        .numeroVente(l.getVente().getNumeroVente())
                        .dateVente(l.getVente().getDateVente())
                        .boutique(l.getVente().getBoutique().getNom())
                        .quantite(l.getQuantite())
                        .prixUnitaire(l.getPrixUnitaire())
                        .sousTotal(l.getSousTotal())
                        .benefice(l.getBenefice())
                        .build())
                .collect(Collectors.toList());

        int quantiteTotale = lignes.stream().mapToInt(LigneVente::getQuantite).sum();
        BigDecimal montantTotal = lignes.stream().map(LigneVente::getSousTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal beneficeTotal = lignes.stream().map(LigneVente::getBenefice).reduce(BigDecimal.ZERO, BigDecimal::add);

        return HistoriqueVenteProduitResponse.builder()
                .codeBarres(produit.getCodeBarres())
                .nomProduit(produit.getNom())
                .periodeDebut(debut)
                .periodeFin(fin)
                .quantiteTotale(quantiteTotale)
                .montantTotal(montantTotal)
                .beneficeTotal(beneficeTotal)
                .ventes(ventes)
                .build();
    }

    // ---------------------------------------------------------------

    private ProduitResponse versReponse(Produit produit, Long idBoutiqueFiltre) {
        List<StockBoutique> stocksEntites = stockBoutiqueRepository.findByProduit_IdProduit(produit.getIdProduit());

        List<StockBoutiqueResponse> stocks = stocksEntites.stream()
                .filter(sb -> idBoutiqueFiltre == null || sb.getBoutique().getIdBoutique().equals(idBoutiqueFiltre))
                .sorted(Comparator.comparing(sb -> sb.getBoutique().getNom()))
                .map(sb -> {
                    int seuil = sb.getSeuilAlerte() != null ? sb.getSeuilAlerte() : produit.getSeuilAlerte();
                    return StockBoutiqueResponse.builder()
                            .idBoutique(sb.getBoutique().getIdBoutique())
                            .nomBoutique(sb.getBoutique().getNom())
                            .quantiteDisponible(sb.getQuantiteDisponible())
                            .prixVente(sb.getPrixVente() != null ? sb.getPrixVente() : produit.getPrixCatalogue())
                            .seuilAlerte(seuil)
                            .enAlerte(sb.getQuantiteDisponible() != null && sb.getQuantiteDisponible() <= seuil)
                            .build();
                })
                .collect(Collectors.toList());

        int total = stocksEntites.stream()
                .filter(sb -> idBoutiqueFiltre == null || sb.getBoutique().getIdBoutique().equals(idBoutiqueFiltre))
                .mapToInt(sb -> sb.getQuantiteDisponible() == null ? 0 : sb.getQuantiteDisponible())
                .sum();

        return ProduitResponse.builder()
                .idProduit(produit.getIdProduit())
                .codeBarres(produit.getCodeBarres())
                .reference(produit.getReference())
                .nom(produit.getNom())
                .description(produit.getDescription())
                .prixAchat(produit.getPrixAchat())
                .prixVente(produit.getPrixCatalogue())
                .seuilAlerte(produit.getSeuilAlerte())
                .image(produit.getImage())
                .poidsG(produit.getPoidsG())
                .volumeMl(produit.getVolumeMl())
                .categorie(produit.getCategorie() != null ? produit.getCategorie().getNom() : null)
                .idCategorie(produit.getCategorie() != null ? produit.getCategorie().getIdCategorie() : null)
                .marque(produit.getMarque() != null ? produit.getMarque().getNom() : null)
                .idMarque(produit.getMarque() != null ? produit.getMarque().getIdMarque() : null)
                .fournisseur(produit.getFournisseur() != null ? produit.getFournisseur().getNom() : null)
                .idFournisseur(produit.getFournisseur() != null ? produit.getFournisseur().getIdFournisseur() : null)
                .actif(produit.getActif())
                .dateCreation(produit.getDateCreation())
                .stocks(stocks)
                .quantiteTotale(total)
                .build();
    }
}
