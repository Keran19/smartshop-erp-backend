package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.MarqueRequest;
import com.smartshop.erp.entity.Marque;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.MarqueRepository;
import com.smartshop.erp.service.MarqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarqueServiceImpl implements MarqueService {

    private final MarqueRepository marqueRepository;

    @Override
    public List<Marque> lister() {
        return marqueRepository.findAll();
    }

    @Override
    public Marque creer(MarqueRequest request) {
        return marqueRepository.save(Marque.builder()
                .nom(request.getNom()).description(request.getDescription()).build());
    }

    @Override
    public Marque modifier(Long id, MarqueRequest request) {
        Marque m = marqueRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Marque introuvable, id=" + id));
        m.setNom(request.getNom());
        m.setDescription(request.getDescription());
        return marqueRepository.save(m);
    }

    @Override
    public void supprimer(Long id) {
        if (!marqueRepository.existsById(id)) {
            throw new RessourceNonTrouveeException("Marque introuvable, id=" + id);
        }
        marqueRepository.deleteById(id);
    }
}
