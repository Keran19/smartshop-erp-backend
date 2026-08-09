package com.smartshop.erp.exception;

/**
 * Levee lorsqu'on cherche un client (ex: par telephone) pour creer un acompte
 * et qu'il n'existe pas encore. Le controller traduit en 404 avec code "CLIENT_INCONNU"
 * pour que le frontend redirige vers la creation du client avant de reprendre l'acompte.
 */
public class ClientIntrouvableException extends RuntimeException {
    public ClientIntrouvableException(String message) {
        super(message);
    }
}
