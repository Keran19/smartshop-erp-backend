package com.smartshop.erp.service.impl;

import com.smartshop.erp.dto.request.ChangerMotDePasseRequest;
import com.smartshop.erp.dto.request.ReinitialiserMotDePasseRequest;
import com.smartshop.erp.dto.request.UtilisateurCreationRequest;
import com.smartshop.erp.dto.request.UtilisateurModificationRequest;
import com.smartshop.erp.dto.response.UtilisateurResponse;
import com.smartshop.erp.entity.Utilisateur;
import com.smartshop.erp.exception.OperationInvalideException;
import com.smartshop.erp.exception.RessourceNonTrouveeException;
import com.smartshop.erp.repository.RefreshTokenRepository;
import com.smartshop.erp.repository.UtilisateurRepository;
import com.smartshop.erp.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UtilisateurResponse> lister() {
        return utilisateurRepository.findAll().stream().map(this::versReponse).collect(Collectors.toList());
    }

    @Override
    public UtilisateurResponse obtenir(Long id) {
        return versReponse(trouver(id));
    }

    @Override
    @Transactional
    public UtilisateurResponse creer(UtilisateurCreationRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new OperationInvalideException("Un compte existe deja avec cet email : " + request.getEmail());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .telephone(request.getTelephone())
                .role(request.getRole())
                .actif(true)
                .doitChangerMotDePasse(true) // un compte cree par un admin doit changer son mot de passe initial
                .build();

        return versReponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional
    public UtilisateurResponse modifier(Long id, UtilisateurModificationRequest request) {
        Utilisateur utilisateur = trouver(id);

        if (!utilisateur.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new OperationInvalideException("Un compte existe deja avec cet email : " + request.getEmail());
        }

        utilisateur.setNom(request.getNom());
        utilisateur.setPrenom(request.getPrenom());
        utilisateur.setEmail(request.getEmail());
        utilisateur.setTelephone(request.getTelephone());
        utilisateur.setRole(request.getRole());
        if (request.getActif() != null) utilisateur.setActif(request.getActif());

        return versReponse(utilisateurRepository.save(utilisateur));
    }

    @Override
    @Transactional
    public void desactiver(Long id) {
        Utilisateur utilisateur = trouver(id);
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
        // Un compte desactive ne doit plus pouvoir utiliser de session existante
        refreshTokenRepository.revoquerTousLesTokensDeLutilisateur(id);
    }

    @Override
    @Transactional
    public void activer(Long id) {
        Utilisateur utilisateur = trouver(id);
        utilisateur.setActif(true);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public void deverrouiller(Long id) {
        Utilisateur utilisateur = trouver(id);
        utilisateur.setVerrouilleJusqua(null);
        utilisateur.setTentativesEchouees(0);
        utilisateurRepository.save(utilisateur);
    }

    @Override
    @Transactional
    public void changerMotDePasse(Long idUtilisateur, ChangerMotDePasseRequest request) {
        Utilisateur utilisateur = trouver(idUtilisateur);

        if (!passwordEncoder.matches(request.getAncienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new BadCredentialsException("L'ancien mot de passe est incorrect");
        }
        if (passwordEncoder.matches(request.getNouveauMotDePasse(), utilisateur.getMotDePasse())) {
            throw new OperationInvalideException("Le nouveau mot de passe doit etre different de l'ancien");
        }

        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setDoitChangerMotDePasse(false);
        utilisateurRepository.save(utilisateur);

        // Par securite, on force une reconnexion sur tous les appareils apres un changement de mot de passe
        refreshTokenRepository.revoquerTousLesTokensDeLutilisateur(idUtilisateur);
    }

    @Override
    @Transactional
    public void reinitialiserMotDePasse(Long idUtilisateur, ReinitialiserMotDePasseRequest request) {
        Utilisateur utilisateur = trouver(idUtilisateur);
        utilisateur.setMotDePasse(passwordEncoder.encode(request.getNouveauMotDePasse()));
        utilisateur.setDoitChangerMotDePasse(true);
        utilisateur.setTentativesEchouees(0);
        utilisateur.setVerrouilleJusqua(null);
        utilisateurRepository.save(utilisateur);
        refreshTokenRepository.revoquerTousLesTokensDeLutilisateur(idUtilisateur);
    }

    // ---------------------------------------------------------------

    private Utilisateur trouver(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new RessourceNonTrouveeException("Utilisateur introuvable, id=" + id));
    }

    private UtilisateurResponse versReponse(Utilisateur u) {
        return UtilisateurResponse.builder()
                .idUtilisateur(u.getIdUtilisateur())
                .nom(u.getNom())
                .prenom(u.getPrenom())
                .email(u.getEmail())
                .telephone(u.getTelephone())
                .role(u.getRole())
                .actif(u.getActif())
                .verrouille(u.estVerrouille())
                .doitChangerMotDePasse(u.getDoitChangerMotDePasse())
                .derniereConnexion(u.getDerniereConnexion())
                .dateCreation(u.getDateCreation())
                .build();
    }
}
