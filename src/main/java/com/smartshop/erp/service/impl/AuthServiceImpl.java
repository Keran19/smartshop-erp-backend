package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.LoginRequest;
import com.smartshop.erp.dto.request.RefreshTokenRequest;
import com.smartshop.erp.dto.response.LoginResponse;
import com.smartshop.erp.entity.JournalAction;
import com.smartshop.erp.entity.RefreshToken;
import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.repository.JournalActionRepository;
import com.smartshop.erp.repository.RefreshTokenRepository;
import com.smartshop.erp.repository.UtilisateurRepository;
import com.smartshop.erp.security.CustomUserDetails;
import com.smartshop.erp.security.JwtService;
import com.smartshop.erp.service.AuthService;
import com.smartshop.erp.service.CompteSecuriteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JournalActionRepository journalActionRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CompteSecuriteService compteSecuriteService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElse(null);

        // Meme message d'erreur, que l'email existe ou non : on ne revele jamais
        // si un compte existe pour un email donne (evite l'enumeration de comptes).
        if (utilisateur == null) {
            journaliser(null, "LOGIN_ECHEC", "Tentative de connexion avec un email inconnu : " + request.getEmail(), httpRequest);
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        if (!Boolean.TRUE.equals(utilisateur.getActif())) {
            journaliser(utilisateur, "LOGIN_ECHEC", "Compte desactive", httpRequest);
            throw new DisabledException("Ce compte a ete desactive. Contactez un administrateur.");
        }

        if (utilisateur.estVerrouille()) {
            String jusqua = utilisateur.getVerrouilleJusqua().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            journaliser(utilisateur, "LOGIN_ECHEC", "Compte verrouille jusqu'a " + jusqua, httpRequest);
            throw new LockedException("Compte temporairement verrouille suite a trop de tentatives echouees. Reessayez apres " + jusqua + ".");
        }

        if (!passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasse())) {
            compteSecuriteService.enregistrerEchec(utilisateur);
            journaliser(utilisateur, "LOGIN_ECHEC", "Mot de passe incorrect (tentative " + (utilisateur.getTentativesEchouees()) + ")", httpRequest);
            throw new BadCredentialsException("Email ou mot de passe incorrect");
        }

        compteSecuriteService.reinitialiserApresSucces(utilisateur);
        journaliser(utilisateur, "LOGIN_SUCCES", "Connexion reussie", httpRequest);

        return construireReponseAvecNouveauxTokens(utilisateur, httpRequest);
    }

    @Override
    @Transactional
    public LoginResponse rafraichir(RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        String type;
        String email;
        try {
            type = jwtService.extraireType(token);
            email = jwtService.extraireEmail(token);
        } catch (Exception e) {
            throw new BadCredentialsException("Refresh token invalide");
        }

        if (!"REFRESH".equals(type)) {
            throw new BadCredentialsException("Le jeton fourni n'est pas un refresh token");
        }

        String hash = jwtService.hasher(token);
        RefreshToken stocke = refreshTokenRepository.findByTokenHash(hash).orElse(null);

        if (stocke == null) {
            throw new BadCredentialsException("Refresh token inconnu ou deja utilise");
        }

        if (!stocke.estValide()) {
            // Reutilisation d'un token deja revoque/expire : indice possible de vol de jeton.
            // Par precaution, on revoque toutes les sessions actives de cet utilisateur.
            refreshTokenRepository.revoquerTousLesTokensDeLutilisateur(stocke.getUtilisateur().getIdUtilisateur());
            throw new BadCredentialsException("Refresh token expire ou revoque. Veuillez vous reconnecter.");
        }

        Utilisateur utilisateur = stocke.getUtilisateur();
        if (!Boolean.TRUE.equals(utilisateur.getActif()) || utilisateur.estVerrouille()) {
            throw new BadCredentialsException("Compte indisponible");
        }

        // Rotation a usage unique : l'ancien refresh token est immediatement invalide.
        stocke.setRevoque(true);
        refreshTokenRepository.save(stocke);

        return construireReponseAvecNouveauxTokens(utilisateur, null);
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        try {
            String hash = jwtService.hasher(request.getRefreshToken());
            refreshTokenRepository.findByTokenHash(hash).ifPresent(rt -> {
                rt.setRevoque(true);
                refreshTokenRepository.save(rt);
            });
        } catch (Exception ignored) {
            // deconnexion idempotente : un token deja invalide ne doit pas provoquer d'erreur
        }
    }

    @Override
    @Transactional
    public void logoutToutesLesSessions(Long idUtilisateur) {
        refreshTokenRepository.revoquerTousLesTokensDeLutilisateur(idUtilisateur);
    }

    // ---------------------------------------------------------------

    private LoginResponse construireReponseAvecNouveauxTokens(Utilisateur utilisateur, HttpServletRequest httpRequest) {
        CustomUserDetails userDetails = new CustomUserDetails(utilisateur);
        String accessToken = jwtService.genererAccessToken(userDetails);
        String refreshToken = jwtService.genererRefreshToken(userDetails);

        RefreshToken entite = RefreshToken.builder()
                .utilisateur(utilisateur)
                .tokenHash(jwtService.hasher(refreshToken))
                .dateExpiration(LocalDateTime.now().plusSeconds(jwtService.getRefreshExpirationMs() / 1000))
                .adresseIp(httpRequest != null ? extraireIp(httpRequest) : null)
                .userAgent(httpRequest != null ? httpRequest.getHeader("User-Agent") : null)
                .build();
        refreshTokenRepository.save(entite);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresInSecondes(900L)
                .idUtilisateur(utilisateur.getIdUtilisateur())
                .nom(utilisateur.getNom())
                .prenom(utilisateur.getPrenom())
                .email(utilisateur.getEmail())
                .role(utilisateur.getRole())
                .doitChangerMotDePasse(utilisateur.getDoitChangerMotDePasse())
                .build();
    }

    private void journaliser(Utilisateur utilisateur, String action, String description, HttpServletRequest httpRequest) {
        JournalAction log = JournalAction.builder()
                .utilisateur(utilisateur)
                .action(action)
                .description(description)
                .adresseIp(httpRequest != null ? extraireIp(httpRequest) : null)
                .build();
        journalActionRepository.save(log);
    }

    private String extraireIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
