package com.smartshop.erp.service;

import com.smartshop.erp.dto.request.LoginRequest;
import com.smartshop.erp.dto.request.RefreshTokenRequest;
import com.smartshop.erp.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /** Echange un refresh token valide contre un nouvel access token (rotation du refresh token inclus). */
    LoginResponse rafraichir(RefreshTokenRequest request);

    /** Revoque le refresh token fourni (deconnexion de la session courante). */
    void logout(RefreshTokenRequest request);

    /** Revoque tous les refresh tokens actifs de l'utilisateur (deconnexion de toutes les sessions). */
    void logoutToutesLesSessions(Long idUtilisateur);
}
