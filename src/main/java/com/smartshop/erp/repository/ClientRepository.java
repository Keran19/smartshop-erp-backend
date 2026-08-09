package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTelephone(String telephone);
    List<Client> findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrTelephoneContaining(
            String nom, String prenom, String telephone);
    long countByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);
    List<Client> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);
}
