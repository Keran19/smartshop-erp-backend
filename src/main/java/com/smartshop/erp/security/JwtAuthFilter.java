package com.smartshop.erp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre execute a chaque requete : extrait le JWT de l'entete Authorization, le valide,
 * et peuple le SecurityContext si le jeton est un ACCESS token valide pour un utilisateur actif.
 * Toute erreur (token absent, invalide, expire, mal type) laisse simplement la requete non
 * authentifiee : c'est ensuite SecurityConfig + CustomAuthenticationEntryPoint qui decident
 * du refus (401) si la route protegee l'exige.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            // Un refresh token ne doit JAMAIS pouvoir servir a authentifier une requete API :
            // seul un ACCESS token est accepte ici.
            if (!"ACCESS".equals(jwtService.extraireType(token))) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = jwtService.extraireEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if (jwtService.estValide(token, userDetails) && userDetails.isEnabled() && userDetails.isAccountNonLocked()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ignored) {
            // token invalide/expire/malforme -> requete laissee non authentifiee
        }

        filterChain.doFilter(request, response);
    }
}
