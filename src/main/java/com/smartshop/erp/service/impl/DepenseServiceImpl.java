package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.DepenseRequest;
import com.smartshop.erp.dto.response.DepenseResponse;
import com.smartshop.erp.entity.Boutique;
import com.smartshop.erp.entity.Depense;
import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.BoutiqueRepository;
import com.smartshop.erp.repository.DepenseRepository;
import com.smartshop.erp.repository.UtilisateurRepository;
import com.smartshop.erp.service.DepenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepenseServiceImpl implements DepenseService {

    private final DepenseRepository depenseRepository;
    private final BoutiqueRepository boutiqueRepository;
    private final UtilisateurRepository utilisateurRepository;

    @Override
    public List<DepenseResponse> lister(LocalDateTime debut, LocalDateTime fin, Long idBoutique) {
        return depenseRepository.findByPeriode(debut, fin, idBoutique).stream()
                .map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepenseResponse creer(DepenseRequest request, Long idUtilisateurConnecte) {
        Boutique boutique = boutiqueRepository.findById(request.getIdBoutique())
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + request.getIdBoutique()));
        Utilisateur utilisateur = utilisateurRepository.findById(idUtilisateurConnecte)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable"));

        Depense depense = Depense.builder()
                .boutique(boutique)
                .libelle(request.getLibelle())
                .categorie(request.getCategorie())
                .montant(request.getMontant())
                .observation(request.getObservation())
                .utilisateur(utilisateur)
                .build();

        return versReponse(depenseRepository.save(depense));
    }

    private DepenseResponse versReponse(Depense d) {
        return DepenseResponse.builder()
                .idDepense(d.getIdDepense())
                .idBoutique(d.getBoutique().getIdBoutique())
                .boutique(d.getBoutique().getNom())
                .libelle(d.getLibelle())
                .categorie(d.getCategorie())
                .montant(d.getMontant())
                .observation(d.getObservation())
                .dateDepense(d.getDateDepense())
                .utilisateur(d.getUtilisateur().getNom() + " " + d.getUtilisateur().getPrenom())
                .build();
    }
}
