package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.response.LotActuelResponse;
import com.smartshop.erp.dto.response.LotStockResponse;
import com.smartshop.erp.entity.HistoriqueApprovisionnement;
import com.smartshop.erp.entity.Produit;
import com.smartshop.erp.entity.StockBoutique;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.HistoriqueApprovisionnementRepository;
import com.smartshop.erp.repository.ProduitRepository;
import com.smartshop.erp.repository.StockBoutiqueRepository;
import com.smartshop.erp.service.CoutMarchandiseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoutMarchandiseServiceImpl implements CoutMarchandiseService {

    private final HistoriqueApprovisionnementRepository historiqueApprovisionnementRepository;
    private final StockBoutiqueRepository stockBoutiqueRepository;
    private final ProduitRepository produitRepository;

    @Override
    @Transactional
    public void deprecierFifo(Long idProduit, Long idBoutique, int quantite) {
        int restantADeduire = quantite;
        List<HistoriqueApprovisionnement> lots = historiqueApprovisionnementRepository
                .lotsDisponiblesFifo(idProduit, idBoutique);

        for (HistoriqueApprovisionnement lot : lots) {
            if (restantADeduire <= 0) break;
            int pris = Math.min(restantADeduire, lot.getQuantiteRestante());
            lot.setQuantiteRestante(lot.getQuantiteRestante() - pris);
            restantADeduire -= pris;
            historiqueApprovisionnementRepository.save(lot);
        }
        // Si restantADeduire > 0 ici, la quantite vendue depasse ce qui a ete enregistre comme
        // approvisionnement pour cette boutique (ex: stock initial saisi hors approvisionnement) ;
        // on ne bloque pas la vente pour autant, le suivi de cout n'est alors plus disponible
        // au-dela des lots connus.
    }

    @Override
    public LotActuelResponse lotActuel(Long idProduit, Long idBoutique) {
        Produit produit = produitRepository.findById(idProduit)
                .orElseThrow(() -> new RessourceNonTrouveeException("Produit introuvable, id=" + idProduit));

        List<HistoriqueApprovisionnement> lots = historiqueApprovisionnementRepository
                .lotsDisponiblesFifo(idProduit, idBoutique);

        BigDecimal prixVenteActuel = stockBoutiqueRepository
                .findByProduit_IdProduitAndBoutique_IdBoutique(idProduit, idBoutique)
                .map(StockBoutique::getPrixVente)
                .orElse(produit.getPrixCatalogue());
        if (prixVenteActuel == null) prixVenteActuel = BigDecimal.ZERO;

        if (lots.isEmpty()) {
            // Aucun lot actif connu : on retombe sur le prix d'achat courant du produit.
            BigDecimal prixAchat = produit.getPrixAchat() == null ? BigDecimal.ZERO : produit.getPrixAchat();
            BigDecimal marge = prixVenteActuel.subtract(prixAchat);
            return LotActuelResponse.builder()
                    .idProduit(produit.getIdProduit())
                    .nomProduit(produit.getNom())
                    .idHistorique(null)
                    .prixAchatLotActuel(prixAchat)
                    .quantiteRestante(0)
                    .dateEntree(null)
                    .fournisseur(null)
                    .prixVenteActuel(prixVenteActuel)
                    .margeUnitaire(marge)
                    .alerteMarge(marge.compareTo(BigDecimal.ZERO) <= 0)
                    .build();
        }

        HistoriqueApprovisionnement lot = lots.get(0);
        BigDecimal marge = prixVenteActuel.subtract(lot.getPrixAchat());

        return LotActuelResponse.builder()
                .idProduit(produit.getIdProduit())
                .nomProduit(produit.getNom())
                .idHistorique(lot.getIdHistorique())
                .prixAchatLotActuel(lot.getPrixAchat())
                .quantiteRestante(lot.getQuantiteRestante())
                .dateEntree(lot.getDateEntree())
                .fournisseur(lot.getFournisseur().getNom())
                .prixVenteActuel(prixVenteActuel)
                .margeUnitaire(marge)
                .alerteMarge(marge.compareTo(BigDecimal.ZERO) <= 0)
                .build();
    }

    @Override
    public List<LotStockResponse> lots(Long idProduit, Long idBoutique) {
        List<HistoriqueApprovisionnement> lots = historiqueApprovisionnementRepository.tousLesLots(idProduit, idBoutique);

        // Le lot "actuel" est le plus ancien qui a encore du stock (donc le premier, en parcourant
        // du plus ancien au plus recent, a avoir quantiteRestante > 0).
        Long idLotActuel = lots.stream()
                .sorted((a, b) -> a.getDateEntree().compareTo(b.getDateEntree()))
                .filter(l -> l.getQuantiteRestante() > 0)
                .map(HistoriqueApprovisionnement::getIdHistorique)
                .findFirst().orElse(null);

        return lots.stream()
                .map(l -> LotStockResponse.builder()
                        .idHistorique(l.getIdHistorique())
                        .numeroApprovisionnement(l.getApprovisionnement().getNumeroApprovisionnement())
                        .fournisseur(l.getFournisseur().getNom())
                        .prixAchat(l.getPrixAchat())
                        .quantiteInitiale(l.getQuantite())
                        .quantiteRestante(l.getQuantiteRestante())
                        .dateEntree(l.getDateEntree())
                        .lotActuel(l.getIdHistorique().equals(idLotActuel))
                        .build())
                .collect(Collectors.toList());
    }
}
