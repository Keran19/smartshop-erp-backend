package com.smartshop.erp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> baseBody(HttpStatus statut, String message, String code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("horodatage", LocalDateTime.now());
        body.put("statut", statut.value());
        body.put("code", code);
        body.put("message", message);
        return body;
    }

    @ExceptionHandler(RessourceNonTrouveeException.class)
    public ResponseEntity<Map<String, Object>> handleRessourceNonTrouvee(RessourceNonTrouveeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(baseBody(HttpStatus.NOT_FOUND, ex.getMessage(), "RESSOURCE_INTROUVABLE"));
    }

    @ExceptionHandler(ProduitIntrouvableParCodeBarresException.class)
    public ResponseEntity<Map<String, Object>> handleProduitInconnu(ProduitIntrouvableParCodeBarresException ex) {
        Map<String, Object> body = baseBody(HttpStatus.NOT_FOUND, ex.getMessage(), "PRODUIT_INCONNU");
        body.put("codeBarres", ex.getCodeBarres());
        body.put("redirection", "AJOUTER_PRODUIT");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(ClientIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> handleClientInconnu(ClientIntrouvableException ex) {
        Map<String, Object> body = baseBody(HttpStatus.NOT_FOUND, ex.getMessage(), "CLIENT_INCONNU");
        body.put("redirection", "CREER_CLIENT");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(StockInsuffisantException.class)
    public ResponseEntity<Map<String, Object>> handleStockInsuffisant(StockInsuffisantException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(baseBody(HttpStatus.CONFLICT, ex.getMessage(), "STOCK_INSUFFISANT"));
    }

    @ExceptionHandler(OperationInvalideException.class)
    public ResponseEntity<Map<String, Object>> handleOperationInvalide(OperationInvalideException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(baseBody(HttpStatus.BAD_REQUEST, ex.getMessage(), "OPERATION_INVALIDE"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = baseBody(HttpStatus.BAD_REQUEST, "Donnees invalides", "VALIDATION_ERREUR");
        Map<String, String> erreurs = new HashMap<>();
        for (FieldError err : ex.getBindingResult().getFieldErrors()) {
            erreurs.put(err.getField(), err.getDefaultMessage());
        }
        body.put("champs", erreurs);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccesRefuse(org.springframework.security.access.AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseBody(HttpStatus.FORBIDDEN, "Acces refuse : privileges insuffisants", "ACCES_REFUSE"));
    }

    @ExceptionHandler(org.springframework.security.authentication.LockedException.class)
    public ResponseEntity<Map<String, Object>> handleCompteVerrouille(org.springframework.security.authentication.LockedException ex) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(baseBody(HttpStatus.LOCKED, ex.getMessage(), "COMPTE_VERROUILLE"));
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<Map<String, Object>> handleCompteDesactive(org.springframework.security.authentication.DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(baseBody(HttpStatus.FORBIDDEN, ex.getMessage(), "COMPTE_DESACTIVE"));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentification(org.springframework.security.core.AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(baseBody(HttpStatus.UNAUTHORIZED, ex.getMessage(), "AUTHENTIFICATION_ECHOUEE"));
    }

    @ExceptionHandler(TropDeRequetesException.class)
    public ResponseEntity<Map<String, Object>> handleTropDeRequetes(TropDeRequetesException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(baseBody(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), "TROP_DE_REQUETES"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenerique(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(baseBody(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne : " + ex.getMessage(), "ERREUR_INTERNE"));
    }
}
