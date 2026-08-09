package com.smartshop.erp.config;

import com.smartshop.erp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Purge periodique des refresh tokens expires, pour eviter que la table refresh_token
 * ne grossisse indefiniment. N'affecte pas la securite (un token expire est deja
 * refuse), c'est uniquement une tache d'hygiene de la base.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NettoyageSecuriteTask {

    private final RefreshTokenRepository refreshTokenRepository;

    /** Tous les jours a 03h00 (heure serveur). */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgerRefreshTokensExpires() {
        refreshTokenRepository.purgerTokensExpires(LocalDateTime.now());
        log.info("Purge des refresh tokens expires effectuee");
    }
}
