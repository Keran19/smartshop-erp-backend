package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.ClientRequest;
import com.smartshop.erp.entity.Client;

import java.util.List;

public interface ClientService {
    List<Client> lister();
    Client obtenir(Long id);
    /** Leve ClientIntrouvableException si aucun client ne correspond a ce telephone (utilise pour le flux acompte). */
    Client obtenirParTelephone(String telephone);
    List<Client> rechercher(String motCle);
    Client creer(ClientRequest request);
    Client modifier(Long id, ClientRequest request);
    void desactiver(Long id);
}
