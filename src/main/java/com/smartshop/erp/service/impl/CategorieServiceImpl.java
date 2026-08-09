package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.CategorieRequest;
import com.smartshop.erp.entity.Categorie;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.CategorieRepository;
import com.smartshop.erp.service.CategorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategorieServiceImpl implements CategorieService {

    private final CategorieRepository categorieRepository;

    @Override
    public List<Categorie> lister() {
        return categorieRepository.findAll();
    }

    @Override
    public Categorie creer(CategorieRequest request) {
        return categorieRepository.save(Categorie.builder()
                .nom(request.getNom()).description(request.getDescription()).build());
    }

    @Override
    public Categorie modifier(Long id, CategorieRequest request) {
        Categorie c = categorieRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Categorie introuvable, id=" + id));
        c.setNom(request.getNom());
        c.setDescription(request.getDescription());
        return categorieRepository.save(c);
    }

    @Override
    public void supprimer(Long id) {
        if (!categorieRepository.existsById(id)) {
            throw new RessourceNonTrouveeException("Categorie introuvable, id=" + id);
        }
        categorieRepository.deleteById(id);
    }
}
