package com.smartshop.erp.repository;

import com.smartshop.erp.entity.Parametre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParametreRepository extends JpaRepository<Parametre, Long> {
    Optional<Parametre> findByCleParametre(String cle);
}
