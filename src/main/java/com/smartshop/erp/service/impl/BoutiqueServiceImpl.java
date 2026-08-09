package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.BoutiqueRequest;
import com.smartshop.erp.entity.Boutique;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.BoutiqueRepository;
import com.smartshop.erp.service.BoutiqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoutiqueServiceImpl implements BoutiqueService {

    private final BoutiqueRepository boutiqueRepository;

    @Override
    public List<Boutique> lister() {
        return boutiqueRepository.findAll();
    }

    @Override
    public Boutique obtenir(Long id) {
        return boutiqueRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Boutique introuvable, id=" + id));
    }

    @Override
    public Boutique creer(BoutiqueRequest request) {
        Boutique boutique = Boutique.builder()
                .nom(request.getNom())
                .adresse(request.getAdresse())
                .telephone(request.getTelephone())
                .principale(Boolean.TRUE.equals(request.getPrincipale()))
                .actif(true)
                .build();
        return boutiqueRepository.save(boutique);
    }

    @Override
    public Boutique modifier(Long id, BoutiqueRequest request) {
        Boutique boutique = obtenir(id);
        boutique.setNom(request.getNom());
        boutique.setAdresse(request.getAdresse());
        boutique.setTelephone(request.getTelephone());
        if (request.getPrincipale() != null) boutique.setPrincipale(request.getPrincipale());
        return boutiqueRepository.save(boutique);
    }

    @Override
    public void desactiver(Long id) {
        Boutique boutique = obtenir(id);
        boutique.setActif(false);
        boutiqueRepository.save(boutique);
    }
}
