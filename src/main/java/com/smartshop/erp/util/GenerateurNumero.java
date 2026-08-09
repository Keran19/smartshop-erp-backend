package com.smartshop.erp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Genere des numeros lisibles de type VEN-20260806-000001, ACP-20260806-000001, etc.
 * Le compteur est reinitialise a chaque redemarrage de l'application mais l'horodatage
 * (jusqu'a la milliseconde) garantit malgre tout l'unicite en pratique. Pour un usage
 * multi-instance, ce compteur pourrait etre remplace par une sequence en base.
 */
public final class GenerateurNumero {

    private static final DateTimeFormatter FORMAT_JOUR = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong COMPTEUR = new AtomicLong(0);

    private GenerateurNumero() {}

    public static String generer(String prefixe) {
        String jour = LocalDateTime.now().format(FORMAT_JOUR);
        long seq = COMPTEUR.incrementAndGet();
        return String.format("%s-%s-%06d", prefixe, jour, seq);
    }
}
