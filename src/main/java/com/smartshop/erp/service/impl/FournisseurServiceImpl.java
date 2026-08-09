package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.FournisseurRequest;
import com.smartshop.erp.entity.Fournisseur;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.FournisseurRepository;
import com.smartshop.erp.service.FournisseurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurServiceImpl implements FournisseurService {

    private final FournisseurRepository fournisseurRepository;

    @Override
    public List<Fournisseur> lister() {
        return fournisseurRepository.findAll();
    }

    @Override
    public Fournisseur obtenir(Long id) {
        return fournisseurRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Fournisseur introuvable, id=" + id));
    }

    @Override
    public Fournisseur creer(FournisseurRequest request) {
        Fournisseur f = Fournisseur.builder()
                .nom(request.getNom())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .adresse(request.getAdresse())
                .actif(true)
                .build();
        return fournisseurRepository.save(f);
    }

    @Override
    public Fournisseur modifier(Long id, FournisseurRequest request) {
        Fournisseur f = obtenir(id);
        f.setNom(request.getNom());
        f.setTelephone(request.getTelephone());
        f.setEmail(request.getEmail());
        f.setAdresse(request.getAdresse());
        return fournisseurRepository.save(f);
    }

    @Override
    public void desactiver(Long id) {
        Fournisseur f = obtenir(id);
        f.setActif(false);
        fournisseurRepository.save(f);
    }
}
