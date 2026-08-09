package com.smartshop.erp.exception;

/** Erreur de logique metier generique (ex: caisse deja ouverte, credit deja solde...). */
public class OperationInvalideException extends RuntimeException {
    public OperationInvalideException(String message) {
        super(message);
    }
}
