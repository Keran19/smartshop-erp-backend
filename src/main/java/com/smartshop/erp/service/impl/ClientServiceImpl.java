package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.ClientRequest;
import com.smartshop.erp.entity.Client;
import com.smartshop.erp.exception.ClientIntrouvableException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.ClientRepository;
import com.smartshop.erp.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    public List<Client> lister() {
        return clientRepository.findAll();
    }

    @Override
    public Client obtenir(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Client introuvable, id=" + id));
    }

    @Override
    public Client obtenirParTelephone(String telephone) {
        return clientRepository.findByTelephone(telephone)
                .orElseThrow(() -> new ClientIntrouvableException(
                        "Aucun client n'est enregistre avec le telephone : " + telephone));
    }

    @Override
    public List<Client> rechercher(String motCle) {
        return clientRepository.findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrTelephoneContaining(
                motCle, motCle, motCle);
    }

    @Override
    public Client creer(ClientRequest request) {
        Client client = Client.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .adresse(request.getAdresse())
                .actif(true)
                .build();
        return clientRepository.save(client);
    }

    @Override
    public Client modifier(Long id, ClientRequest request) {
        Client client = obtenir(id);
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setTelephone(request.getTelephone());
        client.setEmail(request.getEmail());
        client.setAdresse(request.getAdresse());
        return clientRepository.save(client);
    }

    @Override
    public void desactiver(Long id) {
        Client client = obtenir(id);
        client.setActif(false);
        clientRepository.save(client);
    }
}
