package com.smartshop.erp.repository;

import com.smartshop.erp.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    List<RefreshToken> findByUtilisateur_IdUtilisateurAndRevoqueFalse(Long idUtilisateur);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoque = true WHERE r.utilisateur.idUtilisateur = :idUtilisateur AND r.revoque = false")
    void revoquerTousLesTokensDeLutilisateur(@Param("idUtilisateur") Long idUtilisateur);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.dateExpiration < :maintenant")
    void purgerTokensExpires(@Param("maintenant") LocalDateTime maintenant);
}
