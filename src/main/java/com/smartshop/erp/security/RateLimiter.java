package com.smartshop.erp.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Limiteur de debit simple, en memoire, par adresse IP. Protege les endpoints
 * sensibles (/api/auth/login, /api/auth/refresh) contre les attaques par bruteforce
 * distribuees sur plusieurs comptes, en complement du verrouillage de compte
 * (CompteSecuriteService) qui protege un compte donne quelle que soit l'IP.
 *
 * NB : implementation adaptee a une instance unique. Pour un deploiement multi-instance,
 * remplacer par un compteur partage (ex : Redis + bucket4j) afin que la limite soit globale.
 */
@Component
public class RateLimiter {

    private static final int LIMITE_REQUETES = 10;
    private static final long FENETRE_MS = 60_000L; // 1 minute glissante

    private static class Compteur {
        AtomicInteger nombre = new AtomicInteger(0);
        volatile long debutFenetre = Instant.now().toEpochMilli();
    }

    private final ConcurrentHashMap<String, Compteur> compteurs = new ConcurrentHashMap<>();

    /** Retourne true si la requete est autorisee, false si la limite est depassee. */
    public boolean autoriser(String cleIdentifiante) {
        long maintenant = Instant.now().toEpochMilli();
        Compteur compteur = compteurs.computeIfAbsent(cleIdentifiante, k -> new Compteur());

        synchronized (compteur) {
            if (maintenant - compteur.debutFenetre > FENETRE_MS) {
                compteur.debutFenetre = maintenant;
                compteur.nombre.set(0);
            }
            return compteur.nombre.incrementAndGet() <= LIMITE_REQUETES;
        }
    }
}
