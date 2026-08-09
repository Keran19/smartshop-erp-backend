package com.smartshop.erp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Gere la generation et la validation des jetons JWT.
 * Deux types de jetons :
 *  - ACCESS  : courte duree de vie (par defaut 15 min), transporte dans l'entete Authorization,
 *              utilise pour authentifier chaque requete de l'API.
 *  - REFRESH : plus longue duree de vie (par defaut 7 jours), utilise uniquement pour obtenir
 *              un nouveau jeton d'acces via /api/auth/refresh. Son hash est persiste en base
 *              (voir RefreshToken) pour permettre la revocation et la rotation a usage unique.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String genererAccessToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("idUtilisateur", userDetails.getUtilisateur().getIdUtilisateur());
        claims.put("role", userDetails.getUtilisateur().getRole().name());
        claims.put("nom", userDetails.getUtilisateur().getNom());
        claims.put("prenom", userDetails.getUtilisateur().getPrenom());
        claims.put("type", "ACCESS");
        return construireToken(claims, userDetails.getUsername(), accessExpirationMs);
    }

    /** Le refresh token ne porte aucune donnee metier : uniquement l'identite et un type. */
    public String genererRefreshToken(CustomUserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "REFRESH");
        return construireToken(claims, userDetails.getUsername(), refreshExpirationMs);
    }

    private String construireToken(Map<String, Object> claims, String sujet, long dureeMs) {
        Date maintenant = new Date();
        Date expiration = new Date(maintenant.getTime() + dureeMs);
        return Jwts.builder()
                .claims(claims)
                .subject(sujet)
                .issuer("smartshop-erp")
                .issuedAt(maintenant)
                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String extraireEmail(String token) {
        return extraireClaim(token, Claims::getSubject);
    }

    public String extraireType(String token) {
        return extraireClaim(token, c -> c.get("type", String.class));
    }

    public boolean estValide(String token, UserDetails userDetails) {
        try {
            String email = extraireEmail(token);
            return email.equals(userDetails.getUsername()) && !estExpire(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private boolean estExpire(String token) {
        return extraireClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extraireClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return resolver.apply(claims);
    }

    /**
     * Hash SHA-256 du refresh token, utilise comme cle de stockage/recherche en base.
     * On ne persiste jamais le refresh token en clair (meme principe qu'un mot de passe).
     */
    public String hasher(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithme de hachage indisponible", e);
        }
    }
}
