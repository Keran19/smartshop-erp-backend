package com.smartshop.erp.exception;

/** Levee quand une adresse IP depasse la limite de requetes autorisee sur un endpoint sensible. */
public class TropDeRequetesException extends RuntimeException {
    public TropDeRequetesException(String message) {
        super(message);
    }
}
