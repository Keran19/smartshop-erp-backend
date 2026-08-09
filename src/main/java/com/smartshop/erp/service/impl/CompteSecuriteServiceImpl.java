package com.smartshop.erp.service.impl;

import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.repository.UtilisateurRepository;
import com.smartshop.erp.service.CompteSecuriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompteSecuriteServiceImpl implements CompteSecuriteService {

    private final UtilisateurRepository utilisateurRepository;

    @Value("${app.securite.tentatives-max:5}")
    private int tentativesMax;

    @Value("${app.securite.verrouillage-minutes:15}")
    private long verrouillageMinutes;

    @Override
    @Transactional
    public void reinitialiserApresSucces(Utilisateur utilisateur) {
        utilisateur.setTentativesEchouees(0);
        utilisateur.setVerrouilleJusqua(null);
        utilisateur.setDerniereConnexion(LocalDateTime.now());
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public void enregistrerEchec(Utilisateur utilisateur) {
        int tentatives = (utilisateur.getTentativesEchouees() == null ? 0 : utilisateur.getTentativesEchouees()) + 1;
        utilisateur.setTentativesEchouees(tentatives);

        if (tentatives >= tentativesMax) {
            utilisateur.setVerrouilleJusqua(LocalDateTime.now().plusMinutes(verrouillageMinutes));
        }

        utilisateurRepository.save(utilisateur);
    }
}
