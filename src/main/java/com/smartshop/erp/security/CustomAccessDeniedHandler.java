package com.smartshop.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Renvoie un JSON coherent quand un utilisateur authentifie mais non autorise
 * (role insuffisant) tente d'acceder a une ressource bloquee au niveau du filtre
 * de securite (en complement du handler @RestControllerAdvice qui couvre le cas
 * des refus leves depuis les methodes @PreAuthorize).
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("horodatage", LocalDateTime.now());
        body.put("statut", 403);
        body.put("code", "ACCES_REFUSE");
        body.put("message", "Acces refuse : privileges insuffisants pour cette operation");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
