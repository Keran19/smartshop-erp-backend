package com.smartshop.erp.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Intercepte les acces non authentifies a une route protegee et renvoie un JSON
 * coherent avec le reste de l'API (au lieu de la page d'erreur HTML par defaut).
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("horodatage", LocalDateTime.now());
        body.put("statut", 401);
        body.put("code", "NON_AUTHENTIFIE");
        body.put("message", "Authentification requise. Jeton absent, invalide ou expire.");

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
