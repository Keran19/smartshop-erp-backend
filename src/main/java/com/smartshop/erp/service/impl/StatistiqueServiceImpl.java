package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.response.ProduitVenduResponse;
import com.smartshop.erp.dto.response.StatistiqueResponse;
import com.smartshop.erp.enums.StatutVente;
import com.smartshop.erp.repository.ClientRepository;
import com.smartshop.erp.repository.LigneVenteRepository;
import com.smartshop.erp.repository.VenteRepository;
import com.smartshop.erp.service.StatistiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatistiqueServiceImpl implements StatistiqueService {

    private final VenteRepository venteRepository;
    private final LigneVenteRepository ligneVenteRepository;
    private final ClientRepository clientRepository;

    @Override
    public StatistiqueResponse calculer(LocalDateTime debut, LocalDateTime fin, Long idBoutique) {

        BigDecimal chiffreAffaires = venteRepository.sommeChiffreAffaire(debut, fin, StatutVente.VALIDEE, idBoutique);
        long nombreVentes = venteRepository.nombreVentes(debut, fin, StatutVente.VALIDEE, idBoutique);
        BigDecimal beneficeTotal = ligneVenteRepository.beneficeTotalPeriode(debut, fin, idBoutique);
        long nouveauxClients = clientRepository.countByDateCreationBetween(debut, fin);

        List<Object[]> lignesClassement = ligneVenteRepository.produitsLesPlusVendus(debut, fin, idBoutique);

        List<ProduitVenduResponse> classement = lignesClassement.stream()
                .map(row -> ProduitVenduResponse.builder()
                        .idProduit((Long) row[0])
                        .nom((String) row[1])
                        .reference((String) row[2])
                        .codeBarres((String) row[3])
                        .quantiteVendue(((Number) row[4]).longValue())
                        .montantVentes((BigDecimal) row[5])
                        .beneficeGenere((BigDecimal) row[6])
                        .build())
                .collect(Collectors.toList());

        ProduitVenduResponse premier = classement.isEmpty() ? null : classement.get(0);

        return StatistiqueResponse.builder()
                .periodeDebut(debut)
                .periodeFin(fin)
                .idBoutique(idBoutique)
                .chiffreAffaires(chiffreAffaires == null ? BigDecimal.ZERO : chiffreAffaires)
                .nombreVentes(nombreVentes)
                .beneficeTotal(beneficeTotal == null ? BigDecimal.ZERO : beneficeTotal)
                .nombreNouveauxClients(nouveauxClients)
                .produitLePlusVendu(premier)
                .classementProduits(classement)
                .build();
    }
}
