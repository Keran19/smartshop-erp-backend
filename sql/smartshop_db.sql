-- =====================================================
-- SMARTSHOP ERP
-- SCRIPT SQL v3 - CONSOLIDE
-- (multi-boutique, mouvements, acomptes, retours/echanges,
--  benefice a la vente, caisse par coupures CEMAC, securite JWT)
-- =====================================================
-- Ce script remplace entierement les versions precedentes
-- (script initial + patch securite_erp.sql) : il peut etre
-- execute seul sur une base vide.
-- =====================================================

DROP DATABASE IF EXISTS smartshop_db;

CREATE DATABASE smartshop_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smartshop_db;

-- =====================================================
-- UTILISATEURS (+ champs securite anti-bruteforce / hygiene mot de passe)
-- =====================================================

CREATE TABLE utilisateur (
    id_utilisateur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    mot_de_passe VARCHAR(255) NOT NULL,
    telephone VARCHAR(30),
    role ENUM('ADMIN','GERANT','VENDEUR') NOT NULL,
    actif BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,

    -- Securite
    tentatives_echouees INT NOT NULL DEFAULT 0
        COMMENT 'Nombre de tentatives de connexion echouees consecutives',
    verrouille_jusqua DATETIME NULL
        COMMENT 'Si renseigne et dans le futur, le compte est verrouille (anti-bruteforce)',
    doit_changer_mot_de_passe BOOLEAN NOT NULL DEFAULT TRUE
        COMMENT 'Force le changement de mot de passe a la prochaine connexion',
    derniere_connexion DATETIME NULL
);

-- =====================================================
-- CLIENTS
-- =====================================================

CREATE TABLE client(
    id_client BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    prenom VARCHAR(150),
    telephone VARCHAR(30),
    email VARCHAR(150),
    adresse VARCHAR(255),
    actif BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- FOURNISSEURS
-- =====================================================

CREATE TABLE fournisseur(
    id_fournisseur BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    telephone VARCHAR(30),
    email VARCHAR(150),
    adresse VARCHAR(255),
    actif BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- BOUTIQUES (POINTS DE VENTE)
-- =====================================================

CREATE TABLE boutique(
    id_boutique BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    adresse VARCHAR(255),
    telephone VARCHAR(30),
    principale BOOLEAN DEFAULT FALSE,
    actif BOOLEAN DEFAULT TRUE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- CATEGORIES / MARQUES
-- =====================================================

CREATE TABLE categorie(
    id_categorie BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

CREATE TABLE marque(
    id_marque BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- =====================================================
-- PRODUITS (+ prix_achat, poids, volume, fournisseur habituel)
-- =====================================================

CREATE TABLE produit(
    id_produit BIGINT AUTO_INCREMENT PRIMARY KEY,
    code_barres VARCHAR(100) UNIQUE NOT NULL,
    reference VARCHAR(100) UNIQUE,
    nom VARCHAR(200) NOT NULL,
    description TEXT,
    -- Prix d'achat courant (cout) : indispensable pour calculer le benefice a la vente
    prix_achat DECIMAL(12,2) NOT NULL DEFAULT 0,
    -- Prix de vente catalogue (utilise si stock_boutique.prix_vente est NULL)
    prix_catalogue DECIMAL(12,2) NOT NULL,
    seuil_alerte INT DEFAULT 0,
    image VARCHAR(255),
    poids_g DECIMAL(10,2) NULL,
    volume_ml DECIMAL(10,2) NULL,
    id_fournisseur BIGINT NULL,
    actif BOOLEAN DEFAULT TRUE,
    id_categorie BIGINT NOT NULL,
    id_marque BIGINT,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_produit_categorie FOREIGN KEY(id_categorie) REFERENCES categorie(id_categorie),
    CONSTRAINT fk_produit_marque FOREIGN KEY(id_marque) REFERENCES marque(id_marque),
    CONSTRAINT fk_produit_fournisseur FOREIGN KEY(id_fournisseur) REFERENCES fournisseur(id_fournisseur)
);

-- =====================================================
-- HISTORIQUE DES PRIX
-- =====================================================

CREATE TABLE prix_produit(
    id_prix BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    ancien_prix DECIMAL(12,2),
    nouveau_prix DECIMAL(12,2),
    date_modification DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT,

    CONSTRAINT fk_prix_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit) ON DELETE CASCADE,
    CONSTRAINT fk_prix_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- STOCK PAR BOUTIQUE
-- =====================================================

CREATE TABLE stock_boutique(
    id_stock BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    id_boutique BIGINT NOT NULL,
    quantite_disponible INT DEFAULT 0,
    prix_vente DECIMAL(12,2) NULL COMMENT 'si NULL, on utilise produit.prix_catalogue',
    seuil_alerte INT NULL COMMENT 'si NULL, on utilise produit.seuil_alerte',
    actif BOOLEAN DEFAULT TRUE,
    date_mise_a_jour DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sb_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit) ON DELETE CASCADE,
    CONSTRAINT fk_sb_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique) ON DELETE CASCADE,
    CONSTRAINT uq_stock_produit_boutique UNIQUE(id_produit, id_boutique)
);

-- =====================================================
-- MOUVEMENTS DE STOCK
-- (+ RETOUR_CLIENT, SORTIE_ECHANGE pour le module retours)
-- =====================================================

CREATE TABLE mouvement_stock(
    id_mouvement BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    type_mouvement ENUM('ENTREE_APPRO','SORTIE_VENTE','PERTE','TRANSFERT','AJUSTEMENT_INVENTAIRE',
                         'RETOUR_CLIENT','SORTIE_ECHANGE') NOT NULL,
    quantite INT NOT NULL,
    id_boutique_source BIGINT NULL COMMENT 'boutique de depart (perte, transfert, vente, sortie, echange)',
    id_boutique_destination BIGINT NULL COMMENT 'boutique d''arrivee (transfert, entree appro, retour)',
    motif VARCHAR(255),
    date_mouvement DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT,

    CONSTRAINT fk_mouvement_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit),
    CONSTRAINT fk_mouvement_boutique_source FOREIGN KEY(id_boutique_source) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_mouvement_boutique_destination FOREIGN KEY(id_boutique_destination) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_mouvement_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- INDEX (1/5)
-- =====================================================

CREATE INDEX idx_produit_nom ON produit(nom);
CREATE INDEX idx_produit_barcode ON produit(code_barres);
CREATE INDEX idx_produit_reference ON produit(reference);
CREATE INDEX idx_sb_produit ON stock_boutique(id_produit);
CREATE INDEX idx_sb_boutique ON stock_boutique(id_boutique);
CREATE INDEX idx_client_nom ON client(nom);
CREATE INDEX idx_client_tel ON client(telephone);
CREATE INDEX idx_fournisseur_nom ON fournisseur(nom);
CREATE INDEX idx_mouvement_produit ON mouvement_stock(id_produit);
CREATE INDEX idx_mouvement_type ON mouvement_stock(type_mouvement);
CREATE INDEX idx_mouvement_date ON mouvement_stock(date_mouvement);

-- =====================================================
-- VENTES
-- =====================================================

CREATE TABLE vente (
    id_vente BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_vente VARCHAR(50) UNIQUE NOT NULL,
    date_vente DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_boutique BIGINT NOT NULL,
    id_client BIGINT NULL,
    id_vendeur BIGINT NOT NULL,
    montant_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    remise_globale DECIMAL(12,2) DEFAULT 0,
    montant_final DECIMAL(12,2) NOT NULL DEFAULT 0,
    montant_recu DECIMAL(12,2),
    monnaie_rendue DECIMAL(12,2) NOT NULL DEFAULT 0,
    mode_reglement ENUM('COMPTANT','CREDIT') NOT NULL,
    statut ENUM('EN_ATTENTE','VALIDEE','ANNULEE') DEFAULT 'VALIDEE',
    observation TEXT,

    CONSTRAINT fk_vente_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_vente_client FOREIGN KEY(id_client) REFERENCES client(id_client),
    CONSTRAINT fk_vente_vendeur FOREIGN KEY(id_vendeur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- LIGNES DE VENTE (+ prix_achat_unitaire fige -> calcul du benefice)
-- =====================================================

CREATE TABLE ligne_vente (
    id_ligne_vente BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_vente BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(12,2) NOT NULL,
    -- Cout unitaire (prix d'achat) fige au moment de la vente : garantit un calcul de benefice
    -- exact meme si produit.prix_achat change ulterieurement.
    prix_achat_unitaire DECIMAL(12,2) NOT NULL DEFAULT 0,
    sous_total DECIMAL(12,2) NOT NULL,
    -- Colonne calculee : benefice de la ligne = (prix vente - prix achat) x quantite
    benefice DECIMAL(12,2) GENERATED ALWAYS AS ((prix_unitaire - prix_achat_unitaire) * quantite) STORED,

    CONSTRAINT fk_lv_vente FOREIGN KEY(id_vente) REFERENCES vente(id_vente) ON DELETE CASCADE,
    CONSTRAINT fk_lv_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

-- =====================================================
-- FACTURES
-- =====================================================

CREATE TABLE facture (
    id_facture BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_facture VARCHAR(50) UNIQUE NOT NULL,
    id_vente BIGINT UNIQUE NOT NULL,
    date_impression DATETIME DEFAULT CURRENT_TIMESTAMP,
    imprimee BOOLEAN DEFAULT FALSE,

    CONSTRAINT fk_facture_vente FOREIGN KEY(id_vente) REFERENCES vente(id_vente) ON DELETE CASCADE
);

-- =====================================================
-- CREDITS CLIENTS
-- =====================================================

CREATE TABLE credit (
    id_credit BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_vente BIGINT UNIQUE NOT NULL,
    id_client BIGINT NOT NULL,
    montant_initial DECIMAL(12,2) NOT NULL,
    montant_paye DECIMAL(12,2) DEFAULT 0,
    reste_a_payer DECIMAL(12,2) NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    date_limite DATE,
    statut ENUM('EN_COURS','SOLDE','EN_RETARD') DEFAULT 'EN_COURS',
    observation TEXT,

    CONSTRAINT fk_credit_vente FOREIGN KEY(id_vente) REFERENCES vente(id_vente),
    CONSTRAINT fk_credit_client FOREIGN KEY(id_client) REFERENCES client(id_client)
);

-- =====================================================
-- PAIEMENTS DES CREDITS
-- =====================================================

CREATE TABLE paiement_credit (
    id_paiement BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_credit BIGINT NOT NULL,
    montant DECIMAL(12,2) NOT NULL,
    date_paiement DATETIME DEFAULT CURRENT_TIMESTAMP,
    observation TEXT,
    id_utilisateur BIGINT,

    CONSTRAINT fk_paiement_credit FOREIGN KEY(id_credit) REFERENCES credit(id_credit) ON DELETE CASCADE,
    CONSTRAINT fk_paiement_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- ACOMPTES CLIENTS
-- =====================================================

CREATE TABLE acompte (
    id_acompte BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_acompte VARCHAR(50) UNIQUE NOT NULL,
    id_client BIGINT NOT NULL,
    id_boutique BIGINT NOT NULL,
    id_vendeur BIGINT NOT NULL,
    montant_total DECIMAL(12,2) NOT NULL DEFAULT 0,
    montant_verse DECIMAL(12,2) NOT NULL DEFAULT 0,
    reste_a_payer DECIMAL(12,2) NOT NULL DEFAULT 0,
    statut ENUM('EN_ATTENTE','SOLDE','ANNULE') DEFAULT 'EN_ATTENTE',
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    observation TEXT,

    CONSTRAINT fk_acompte_client FOREIGN KEY(id_client) REFERENCES client(id_client),
    CONSTRAINT fk_acompte_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_acompte_vendeur FOREIGN KEY(id_vendeur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE ligne_acompte (
    id_ligne_acompte BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_acompte BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(12,2) NOT NULL,
    sous_total DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_la_acompte FOREIGN KEY(id_acompte) REFERENCES acompte(id_acompte) ON DELETE CASCADE,
    CONSTRAINT fk_la_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

CREATE TABLE versement_acompte (
    id_versement BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_acompte BIGINT NOT NULL,
    montant DECIMAL(12,2) NOT NULL,
    date_versement DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT,
    observation TEXT,

    CONSTRAINT fk_va_acompte FOREIGN KEY(id_acompte) REFERENCES acompte(id_acompte) ON DELETE CASCADE,
    CONSTRAINT fk_va_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- RETOURS CLIENTS (remboursement / echange meme valeur / echange valeur differente)
-- =====================================================

CREATE TABLE retour (
    id_retour BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_retour VARCHAR(50) UNIQUE NOT NULL,
    id_vente BIGINT NOT NULL COMMENT 'vente d''origine, jamais modifiee par le retour',
    id_boutique BIGINT NOT NULL,
    id_utilisateur BIGINT NOT NULL COMMENT 'agent ayant traite le retour',
    type_retour ENUM('REMBOURSEMENT','ECHANGE_MEME_VALEUR','ECHANGE_VALEUR_DIFFERENTE') NOT NULL,
    montant_retourne DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'valeur des produits rendus',
    montant_echange DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'valeur des nouveaux produits donnes (0 si remboursement)',
    montant_rembourse DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'somme rendue au client',
    montant_complement DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT 'somme payee en plus par le client',
    statut ENUM('VALIDE','ANNULE') DEFAULT 'VALIDE',
    date_retour DATETIME DEFAULT CURRENT_TIMESTAMP,
    observation TEXT,

    CONSTRAINT fk_retour_vente FOREIGN KEY(id_vente) REFERENCES vente(id_vente),
    CONSTRAINT fk_retour_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_retour_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE ligne_retour (
    id_ligne_retour BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_retour BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(12,2) NOT NULL COMMENT 'prix auquel le produit avait ete vendu',
    sous_total DECIMAL(12,2) NOT NULL,
    motif VARCHAR(255),

    CONSTRAINT fk_lr_retour FOREIGN KEY(id_retour) REFERENCES retour(id_retour) ON DELETE CASCADE,
    CONSTRAINT fk_lr_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

CREATE TABLE ligne_echange (
    id_ligne_echange BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_retour BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_unitaire DECIMAL(12,2) NOT NULL,
    sous_total DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_le_retour FOREIGN KEY(id_retour) REFERENCES retour(id_retour) ON DELETE CASCADE,
    CONSTRAINT fk_le_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

-- =====================================================
-- INDEX (2/5)
-- =====================================================

CREATE INDEX idx_vente_date ON vente(date_vente);
CREATE INDEX idx_vente_boutique ON vente(id_boutique);
CREATE INDEX idx_vente_client ON vente(id_client);
CREATE INDEX idx_vente_vendeur ON vente(id_vendeur);
CREATE INDEX idx_credit_client ON credit(id_client);
CREATE INDEX idx_credit_statut ON credit(statut);
CREATE INDEX idx_ligne_vente ON ligne_vente(id_vente);
CREATE INDEX idx_ligne_vente_produit ON ligne_vente(id_produit);
CREATE INDEX idx_facture_numero ON facture(numero_facture);
CREATE INDEX idx_acompte_client ON acompte(id_client);
CREATE INDEX idx_acompte_statut ON acompte(statut);
CREATE INDEX idx_retour_vente ON retour(id_vente);
CREATE INDEX idx_retour_boutique ON retour(id_boutique);
CREATE INDEX idx_retour_date ON retour(date_retour);
CREATE INDEX idx_ligne_retour_retour ON ligne_retour(id_retour);
CREATE INDEX idx_ligne_retour_produit ON ligne_retour(id_produit);
CREATE INDEX idx_ligne_echange_retour ON ligne_echange(id_retour);

-- =====================================================
-- APPROVISIONNEMENTS
-- =====================================================

CREATE TABLE approvisionnement (
    id_approvisionnement BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_approvisionnement VARCHAR(50) UNIQUE NOT NULL,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_fournisseur BIGINT NOT NULL,
    id_boutique BIGINT NOT NULL,
    id_gerant BIGINT NOT NULL,
    montant_total DECIMAL(12,2) DEFAULT 0,
    statut ENUM('EN_ATTENTE','RECU','ANNULE') DEFAULT 'EN_ATTENTE',
    observation TEXT,

    CONSTRAINT fk_appro_fournisseur FOREIGN KEY(id_fournisseur) REFERENCES fournisseur(id_fournisseur),
    CONSTRAINT fk_appro_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_appro_gerant FOREIGN KEY(id_gerant) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE ligne_approvisionnement (
    id_ligne_approvisionnement BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_approvisionnement BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_achat DECIMAL(12,2) NOT NULL,
    sous_total DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_la_appro FOREIGN KEY(id_approvisionnement) REFERENCES approvisionnement(id_approvisionnement) ON DELETE CASCADE,
    CONSTRAINT fk_la2_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

CREATE TABLE historique_approvisionnement (
    id_historique BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    id_fournisseur BIGINT NOT NULL,
    id_approvisionnement BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_achat DECIMAL(12,2) NOT NULL,
    date_entree DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_hist_prod FOREIGN KEY(id_produit) REFERENCES produit(id_produit),
    CONSTRAINT fk_hist_four FOREIGN KEY(id_fournisseur) REFERENCES fournisseur(id_fournisseur),
    CONSTRAINT fk_hist_appro FOREIGN KEY(id_approvisionnement) REFERENCES approvisionnement(id_approvisionnement)
);

-- =====================================================
-- INVENTAIRE
-- =====================================================

CREATE TABLE inventaire (
    id_inventaire BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_boutique BIGINT NOT NULL,
    date_inventaire DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT NOT NULL,
    observation TEXT,

    CONSTRAINT fk_inv_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_inv_user FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE ligne_inventaire (
    id_ligne BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_inventaire BIGINT NOT NULL,
    id_produit BIGINT NOT NULL,
    quantite_theorique INT NOT NULL,
    quantite_physique INT NOT NULL,
    ecart INT NOT NULL,

    CONSTRAINT fk_li_inv FOREIGN KEY(id_inventaire) REFERENCES inventaire(id_inventaire) ON DELETE CASCADE,
    CONSTRAINT fk_li_prod FOREIGN KEY(id_produit) REFERENCES produit(id_produit)
);

-- =====================================================
-- ALERTES DE STOCK
-- =====================================================

CREATE TABLE alerte_stock (
    id_alerte BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    id_boutique BIGINT NOT NULL,
    quantite_restante INT NOT NULL,
    date_alerte DATETIME DEFAULT CURRENT_TIMESTAMP,
    statut ENUM('NON_LUE','LUE') DEFAULT 'NON_LUE',

    CONSTRAINT fk_alerte_prod FOREIGN KEY(id_produit) REFERENCES produit(id_produit),
    CONSTRAINT fk_alerte_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique)
);

-- =====================================================
-- ETIQUETTES IMPRIMEES
-- =====================================================

CREATE TABLE etiquette (
    id_etiquette BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produit BIGINT NOT NULL,
    quantite_imprimee INT NOT NULL,
    date_generation DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT,

    CONSTRAINT fk_etiquette_produit FOREIGN KEY(id_produit) REFERENCES produit(id_produit),
    CONSTRAINT fk_etiquette_user FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- INDEX (3/5)
-- =====================================================

CREATE INDEX idx_appro_date ON approvisionnement(date_creation);
CREATE INDEX idx_appro_four ON approvisionnement(id_fournisseur);
CREATE INDEX idx_appro_boutique ON approvisionnement(id_boutique);
CREATE INDEX idx_ligne_appro ON ligne_approvisionnement(id_approvisionnement);
CREATE INDEX idx_hist_prod ON historique_approvisionnement(id_produit);
CREATE INDEX idx_hist_date ON historique_approvisionnement(date_entree);
CREATE INDEX idx_alerte ON alerte_stock(id_produit);
CREATE INDEX idx_alerte_boutique ON alerte_stock(id_boutique);

-- =====================================================
-- DEPENSES / NOTIFICATIONS / JOURNAL / PARAMETRES / SAUVEGARDES
-- =====================================================

CREATE TABLE depense(
    id_depense BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_boutique BIGINT NOT NULL,
    libelle VARCHAR(150) NOT NULL,
    categorie VARCHAR(100),
    montant DECIMAL(12,2) NOT NULL,
    observation TEXT,
    date_depense DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT NOT NULL,

    CONSTRAINT fk_depense_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_depense_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE notification(
    id_notification BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    type ENUM('INFO','ALERTE','ERREUR') DEFAULT 'INFO',
    vue BOOLEAN DEFAULT FALSE,
    date_creation DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_utilisateur BIGINT,

    CONSTRAINT fk_notification_user FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE journal_action(
    id_action BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur BIGINT,
    action VARCHAR(255),
    description TEXT,
    adresse_ip VARCHAR(50),
    date_action DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_action_user FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

CREATE TABLE parametre(
    id_parametre BIGINT AUTO_INCREMENT PRIMARY KEY,
    cle_parametre VARCHAR(150) UNIQUE,
    valeur TEXT,
    description TEXT
);

CREATE TABLE sauvegarde(
    id_sauvegarde BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom_fichier VARCHAR(255),
    date_sauvegarde DATETIME DEFAULT CURRENT_TIMESTAMP,
    taille_fichier BIGINT,
    effectue_par BIGINT,

    CONSTRAINT fk_sauvegarde_user FOREIGN KEY(effectue_par) REFERENCES utilisateur(id_utilisateur)
);

-- =====================================================
-- INDEX (4/5)
-- =====================================================

CREATE INDEX idx_depense_date ON depense(date_depense);
CREATE INDEX idx_depense_user ON depense(id_utilisateur);
CREATE INDEX idx_depense_boutique ON depense(id_boutique);
CREATE INDEX idx_notification_user ON notification(id_utilisateur);
CREATE INDEX idx_notification_vue ON notification(vue);
CREATE INDEX idx_action_date ON journal_action(date_action);
CREATE INDEX idx_sauvegarde_date ON sauvegarde(date_sauvegarde);

-- =====================================================
-- SESSION DE CAISSE (+ coupures CEMAC a l'ouverture ET a la fermeture)
-- Remplace l'ancien couple cloture_caisse / detail_coupure.
-- =====================================================

CREATE TABLE session_caisse(
    id_session BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_boutique BIGINT NOT NULL,
    id_utilisateur BIGINT NOT NULL,
    date_ouverture DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_fermeture DATETIME,
    fond_caisse DECIMAL(12,2) DEFAULT 0 COMMENT 'montant declare a l''ouverture (calcule depuis les coupures)',
    montant_theorique DECIMAL(12,2) DEFAULT 0 COMMENT 'fond_caisse + ventes especes - depenses',
    montant_compte DECIMAL(12,2) DEFAULT 0 COMMENT 'montant reellement compte a la fermeture',
    ecart DECIMAL(12,2) DEFAULT 0,
    statut ENUM('OUVERTE','FERMEE') DEFAULT 'OUVERTE',
    observation TEXT,

    CONSTRAINT fk_session_boutique FOREIGN KEY(id_boutique) REFERENCES boutique(id_boutique),
    CONSTRAINT fk_session_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur)
);

-- Detail des coupures (billets/pieces) de la zone CEMAC (XAF), saisies a l'ouverture ET a la
-- fermeture. Billets CEMAC en circulation : 10000, 5000, 2000, 1000, 500. Les pieces (500, 100,
-- 50, 25, 10, 5, 1 FCFA) sont saisies sous forme d'un montant total.
CREATE TABLE detail_coupure_session(
    id_detail BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_session BIGINT NOT NULL,
    type_operation ENUM('OUVERTURE','FERMETURE') NOT NULL,
    billet_10000 INT DEFAULT 0,
    billet_5000 INT DEFAULT 0,
    billet_2000 INT DEFAULT 0,
    billet_1000 INT DEFAULT 0,
    billet_500 INT DEFAULT 0,
    pieces DECIMAL(12,2) DEFAULT 0,
    total DECIMAL(12,2) NOT NULL,

    CONSTRAINT fk_detail_coupure_session FOREIGN KEY(id_session) REFERENCES session_caisse(id_session) ON DELETE CASCADE
);

CREATE INDEX idx_session_user ON session_caisse(id_utilisateur);
CREATE INDEX idx_session_boutique ON session_caisse(id_boutique);
CREATE INDEX idx_session_date ON session_caisse(date_ouverture);
CREATE INDEX idx_detail_coupure_session ON detail_coupure_session(id_session);

-- =====================================================
-- REFRESH TOKENS (JWT) - jamais stockes en clair, seul le hash SHA-256 est persiste
-- =====================================================

CREATE TABLE refresh_token (
    id_refresh_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_utilisateur BIGINT NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_expiration DATETIME NOT NULL,
    revoque BOOLEAN NOT NULL DEFAULT FALSE,
    adresse_ip VARCHAR(50),
    user_agent VARCHAR(255),

    CONSTRAINT fk_refresh_token_utilisateur FOREIGN KEY(id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_token_utilisateur ON refresh_token(id_utilisateur);
CREATE INDEX idx_refresh_token_expiration ON refresh_token(date_expiration);
CREATE INDEX idx_refresh_token_revoque ON refresh_token(revoque);

-- =====================================================
-- INDEX (5/5) COMPLEMENTAIRES
-- =====================================================

CREATE INDEX idx_facture ON facture(numero_facture);

-- ============================================================
-- TRIGGER : SORTIE STOCK SUR VENTE (par boutique)
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_sortie_stock
AFTER INSERT ON ligne_vente
FOR EACH ROW
BEGIN
    DECLARE v_boutique BIGINT;

    SELECT id_boutique INTO v_boutique FROM vente WHERE id_vente = NEW.id_vente;

    UPDATE stock_boutique
    SET quantite_disponible = quantite_disponible - NEW.quantite,
        date_mise_a_jour = NOW()
    WHERE id_produit = NEW.id_produit AND id_boutique = v_boutique;

    INSERT INTO mouvement_stock(id_produit, type_mouvement, quantite, id_boutique_source, motif)
    VALUES(NEW.id_produit, 'SORTIE_VENTE', NEW.quantite, v_boutique, CONCAT('Vente #', NEW.id_vente));
END$$

DELIMITER ;

-- ============================================================
-- TRIGGER : ENTREE STOCK SUR APPROVISIONNEMENT (par boutique)
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_entree_stock
AFTER INSERT ON ligne_approvisionnement
FOR EACH ROW
BEGIN
    DECLARE v_boutique BIGINT;

    SELECT id_boutique INTO v_boutique FROM approvisionnement WHERE id_approvisionnement = NEW.id_approvisionnement;

    INSERT INTO stock_boutique(id_produit, id_boutique, quantite_disponible)
    VALUES(NEW.id_produit, v_boutique, NEW.quantite)
    ON DUPLICATE KEY UPDATE
        quantite_disponible = quantite_disponible + NEW.quantite,
        date_mise_a_jour = NOW();

    INSERT INTO mouvement_stock(id_produit, type_mouvement, quantite, id_boutique_destination, motif)
    VALUES(NEW.id_produit, 'ENTREE_APPRO', NEW.quantite, v_boutique, CONCAT('Approvisionnement #', NEW.id_approvisionnement));
END$$

DELIMITER ;

-- ============================================================
-- TRIGGER : HISTORIQUE DES PRIX
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_historique_prix
AFTER UPDATE ON produit
FOR EACH ROW
BEGIN
    IF OLD.prix_catalogue <> NEW.prix_catalogue THEN
        INSERT INTO prix_produit(id_produit, ancien_prix, nouveau_prix, date_modification)
        VALUES(NEW.id_produit, OLD.prix_catalogue, NEW.prix_catalogue, NOW());
    END IF;
END$$

DELIMITER ;

-- ============================================================
-- TRIGGER : MISE A JOUR CREDIT
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_credit
AFTER INSERT ON paiement_credit
FOR EACH ROW
BEGIN
    UPDATE credit
    SET montant_paye = montant_paye + NEW.montant,
        reste_a_payer = reste_a_payer - NEW.montant
    WHERE id_credit = NEW.id_credit;

    UPDATE credit
    SET statut = 'SOLDE'
    WHERE id_credit = NEW.id_credit AND reste_a_payer <= 0;
END$$

DELIMITER ;

-- ============================================================
-- TRIGGER : MISE A JOUR ACOMPTE
-- ============================================================

DELIMITER $$

CREATE TRIGGER trg_acompte
AFTER INSERT ON versement_acompte
FOR EACH ROW
BEGIN
    UPDATE acompte
    SET montant_verse = montant_verse + NEW.montant,
        reste_a_payer = reste_a_payer - NEW.montant
    WHERE id_acompte = NEW.id_acompte;

    UPDATE acompte
    SET statut = 'SOLDE'
    WHERE id_acompte = NEW.id_acompte AND reste_a_payer <= 0;
END$$

DELIMITER ;

-- ============================================================
-- PROCEDURES DE MOUVEMENT DE STOCK
-- ============================================================

DELIMITER $$

CREATE PROCEDURE sp_transferer_stock(
    IN p_id_produit BIGINT,
    IN p_boutique_origine BIGINT,
    IN p_boutique_destination BIGINT,
    IN p_quantite INT,
    IN p_id_utilisateur BIGINT
)
BEGIN
    UPDATE stock_boutique
    SET quantite_disponible = quantite_disponible - p_quantite, date_mise_a_jour = NOW()
    WHERE id_produit = p_id_produit AND id_boutique = p_boutique_origine;

    INSERT INTO stock_boutique(id_produit, id_boutique, quantite_disponible)
    VALUES(p_id_produit, p_boutique_destination, p_quantite)
    ON DUPLICATE KEY UPDATE
        quantite_disponible = quantite_disponible + p_quantite,
        date_mise_a_jour = NOW();

    INSERT INTO mouvement_stock(id_produit, type_mouvement, quantite, id_boutique_source, id_boutique_destination, id_utilisateur, motif)
    VALUES(p_id_produit, 'TRANSFERT', p_quantite, p_boutique_origine, p_boutique_destination, p_id_utilisateur, 'Transfert entre boutiques');
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE sp_declarer_perte(
    IN p_id_produit BIGINT,
    IN p_boutique BIGINT,
    IN p_quantite INT,
    IN p_motif VARCHAR(255),
    IN p_id_utilisateur BIGINT
)
BEGIN
    UPDATE stock_boutique
    SET quantite_disponible = quantite_disponible - p_quantite, date_mise_a_jour = NOW()
    WHERE id_produit = p_id_produit AND id_boutique = p_boutique;

    INSERT INTO mouvement_stock(id_produit, type_mouvement, quantite, id_boutique_source, id_utilisateur, motif)
    VALUES(p_id_produit, 'PERTE', p_quantite, p_boutique, p_id_utilisateur, p_motif);
END$$

DELIMITER ;

-- ============================================================
-- VUES
-- ============================================================

CREATE VIEW v_produits_plus_vendus AS
SELECT p.id_produit, p.reference, p.nom,
       SUM(l.quantite) AS quantite_vendue,
       SUM(l.sous_total) AS montant,
       SUM(l.benefice) AS benefice
FROM produit p
JOIN ligne_vente l ON p.id_produit = l.id_produit
GROUP BY p.id_produit, p.reference, p.nom;

CREATE VIEW v_chiffre_affaire AS
SELECT DATE(date_vente) AS jour,
       COUNT(*) AS nombre_ventes,
       SUM(montant_final) AS chiffre_affaire
FROM vente
WHERE statut = 'VALIDEE'
GROUP BY DATE(date_vente);

CREATE VIEW v_clients_dettes AS
SELECT c.id_client, c.nom, c.prenom,
       cr.montant_initial, cr.montant_paye, cr.reste_a_payer, cr.statut
FROM client c
JOIN credit cr ON c.id_client = cr.id_client;

CREATE VIEW v_stock_faible AS
SELECT sb.id_boutique, b.nom AS boutique, p.id_produit, p.reference, p.nom,
       sb.quantite_disponible,
       COALESCE(sb.seuil_alerte, p.seuil_alerte) AS seuil_alerte
FROM stock_boutique sb
JOIN produit p ON p.id_produit = sb.id_produit
JOIN boutique b ON b.id_boutique = sb.id_boutique
WHERE sb.quantite_disponible <= COALESCE(sb.seuil_alerte, p.seuil_alerte);

CREATE VIEW v_produits_par_boutique AS
SELECT sb.id_boutique, b.nom AS boutique, p.id_produit, p.reference, p.nom,
       sb.actif,
       COALESCE(sb.prix_vente, p.prix_catalogue) AS prix_vente,
       p.prix_achat,
       sb.quantite_disponible,
       COALESCE(sb.seuil_alerte, p.seuil_alerte) AS seuil_alerte,
       (sb.quantite_disponible <= COALESCE(sb.seuil_alerte, p.seuil_alerte)) AS en_alerte
FROM stock_boutique sb
JOIN produit p ON p.id_produit = sb.id_produit
JOIN boutique b ON b.id_boutique = sb.id_boutique;

CREATE VIEW v_rapport_caisse AS
SELECT DATE(v.date_vente) AS date_rapport, v.id_boutique,
       COUNT(v.id_vente) AS nombre_factures,
       SUM(v.montant_final) AS chiffre_affaire,
       SUM(CASE WHEN v.mode_reglement = 'COMPTANT' THEN v.montant_final ELSE 0 END) AS ventes_comptant,
       SUM(CASE WHEN v.mode_reglement = 'CREDIT' THEN v.montant_final ELSE 0 END) AS ventes_credit
FROM vente v
WHERE v.statut = 'VALIDEE'
GROUP BY DATE(v.date_vente), v.id_boutique;

CREATE VIEW v_depenses AS
SELECT DATE(date_depense) AS jour, SUM(montant) AS montant
FROM depense
GROUP BY DATE(date_depense);

CREATE VIEW v_etat_mouvements_stock AS
SELECT m.id_mouvement, m.date_mouvement, p.nom AS produit, m.type_mouvement,
       m.quantite, bs.nom AS boutique_source, bd.nom AS boutique_destination,
       m.motif, u.nom AS utilisateur
FROM mouvement_stock m
JOIN produit p ON p.id_produit = m.id_produit
LEFT JOIN boutique bs ON bs.id_boutique = m.id_boutique_source
LEFT JOIN boutique bd ON bd.id_boutique = m.id_boutique_destination
LEFT JOIN utilisateur u ON u.id_utilisateur = m.id_utilisateur;

-- Recapitulatif des retours, avec le detail financier (rembourse / complement)
CREATE VIEW v_retours AS
SELECT r.id_retour, r.numero_retour, r.date_retour, r.type_retour, r.statut,
       v.numero_vente, b.nom AS boutique, u.nom AS agent,
       r.montant_retourne, r.montant_echange, r.montant_rembourse, r.montant_complement
FROM retour r
JOIN vente v ON v.id_vente = r.id_vente
JOIN boutique b ON b.id_boutique = r.id_boutique
JOIN utilisateur u ON u.id_utilisateur = r.id_utilisateur;

-- ============================================================
-- PROCEDURES DE RAPPORT
-- ============================================================

DELIMITER $$

CREATE PROCEDURE rapport_journalier(IN p_date DATE)
BEGIN
    SELECT * FROM v_rapport_caisse WHERE date_rapport = p_date;
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE rapport_mensuel(IN p_mois INT, IN p_annee INT)
BEGIN
    SELECT MONTH(date_vente) AS mois, YEAR(date_vente) AS annee,
           COUNT(*) AS ventes, SUM(montant_final) AS chiffre_affaire
    FROM vente
    WHERE MONTH(date_vente) = p_mois AND YEAR(date_vente) = p_annee
      AND statut = 'VALIDEE'
    GROUP BY MONTH(date_vente), YEAR(date_vente);
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE rapport_periode(IN p_date_debut DATE, IN p_date_fin DATE)
BEGIN
    SELECT COUNT(*) AS nombre_ventes_total, SUM(montant_final) AS chiffre_affaire_total
    FROM vente
    WHERE DATE(date_vente) BETWEEN p_date_debut AND p_date_fin AND statut = 'VALIDEE';

    SELECT * FROM v_chiffre_affaire
    WHERE jour BETWEEN p_date_debut AND p_date_fin
    ORDER BY jour;
END$$

DELIMITER ;

DELIMITER $$

CREATE PROCEDURE etat_credits()
BEGIN
    SELECT * FROM v_clients_dettes;
END$$

DELIMITER ;

-- ============================================================
-- DONNEES INITIALES
-- ============================================================

INSERT INTO boutique(nom, principale) VALUES ('Boutique principale', TRUE);

INSERT INTO categorie(nom) VALUES
('Boissons'),('Alimentation'),('Hygiène'),('Cosmétiques'),('Électronique'),('Divers');

INSERT INTO marque(nom) VALUES ('Sans marque');

INSERT INTO parametre(cle_parametre, valeur, description) VALUES
('nom_boutique','JOVAL','Nom de la boutique'),
('devise','FCFA','Devise'),
('taux_tva','0','TVA'),
('telephone','','Téléphone'),
('adresse','','Adresse'),
('message_ticket','Merci de votre confiance.','Message imprimé');

-- Compte ADMIN initial : mot de passe temporaire "ChangeMoi123" (hache BCrypt, cout 12).
-- doit_changer_mot_de_passe = TRUE force son changement des la premiere connexion.
-- IMPORTANT : regenerez ce hash avant toute mise en production
-- (new BCryptPasswordEncoder(12).encode("VotreMotDePasseTemporaire")).
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, role, actif, doit_changer_mot_de_passe)
VALUES (
    'Administrateur',
    'Systeme',
    'admin@smartshop.local',
    '$2a$12$5Qm8N2r0kQhF1t2N1a3XeOe1qkq0V6Zc0e6xXG1sVYV1o3E1Zc1yG',
    'ADMIN',
    TRUE,
    TRUE
);

-- ============================================================
-- NOTES D'EXPLOITATION
-- ============================================================
-- - Purger periodiquement les refresh tokens expires :
--     DELETE FROM refresh_token WHERE date_expiration < NOW();
--   (deja automatise cote application via NettoyageSecuriteTask, tous les jours a 03h00)
-- - Le stock n'est jamais verifie contre le negatif par les triggers SQL eux-memes :
--   c'est la couche service Java (VenteServiceImpl, RetourServiceImpl) qui valide la
--   disponibilite AVANT d'inserer les lignes qui declenchent ces triggers.
