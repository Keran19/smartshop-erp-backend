# SmartShop ERP - Backend

Backend Spring Boot 3 / Java 21 / Maven pour la gestion multi-boutique :
produits & stock, ventes (panier de caisse), retours/echanges, credits,
acomptes, statistiques, et securite JWT professionnelle.

## Demarrage

1. **Base de donnees** : creer le schema en executant le script SQL unique :
   ```
   mysql -u root -p < sql/smartshop_db.sql
   ```
   Ce script cree toutes les tables, triggers, procedures et vues, ainsi
   qu'un compte administrateur initial :
   - email : `admin@smartshop.local`
   - mot de passe temporaire : `ChangeMoi123`
   - **IMPORTANT** : regenerez le hash BCrypt de ce compte avant toute mise
     en production (voir commentaire dans le script SQL). Le compte est
     configure pour forcer un changement de mot de passe des la premiere
     connexion (`doit_changer_mot_de_passe = TRUE`).

2. **Configuration** : ajustez `src/main/resources/application.properties`
   (URL de connexion MySQL, ou surchargez via variables d'environnement) :
   - `APP_JWT_SECRET` : cle secrete JWT (obligatoire en production, >= 32 caracteres)
   - `APP_CORS_ORIGINES` : liste des origines frontend autorisees

3. **Lancement** :
   ```
   mvn spring-boot:run
   ```
   ou construire le jar :
   ```
   mvn clean package
   java -jar target/smartshop-erp.jar
   ```

## Securite

- Authentification JWT avec **access token** (15 min) et **refresh token**
  (7 jours, hache SHA-256 en base, revocable, rotation a usage unique).
- Verrouillage de compte apres 5 echecs de connexion (configurable).
- Rate limiting par IP sur `/api/auth/login` et `/api/auth/refresh`.
- Mots de passe forts obligatoires (8+ caracteres, majuscule, minuscule, chiffre).
- Autorisations par role (ADMIN / GERANT / VENDEUR) via `@PreAuthorize`.
- En-tetes de securite HTTP (HSTS, no-frame, referrer-policy), CORS restreint.

Voir `POST /api/auth/login`, `POST /api/auth/refresh`, `POST /api/auth/logout`.

## Fonctionnalites principales

- **Produits** (`/api/produits`) : liste avec disponibilite en stock par
  boutique, creation avec prix d'achat + prix de vente, recherche par
  code-barres (`/scan/{codeBarres}`) avec redirection automatique vers la
  creation si le produit est inconnu, historique de vente d'un produit par
  code-barres sur une periode (`/scan/{codeBarres}/historique-ventes`).
- **Ventes / panier de caisse** (`/api/ventes`) : `/apercu` calcule les
  totaux et la monnaie a rendre sans rien enregistrer (ecran de confirmation
  avant impression) ; `POST /api/ventes` valide definitivement (deduit le
  stock, cree facture et credit eventuel) ; `/{id}/imprimer` genere et
  telecharge la facture PDF ; `/historique` et `/historique/pdf` listent /
  exportent les ventes d'une periode choisie, avec le benefice de chaque vente.
- **Retours clients** (`/api/retours`) : remboursement, echange a valeur
  egale, echange a valeur differente (avec calcul automatique du complement
  a payer ou du montant a rembourser), mise a jour automatique du stock.
- **Acomptes** (`/api/acomptes`) : creation liee a un client existant (voir
  `GET /api/clients/telephone/{telephone}` pour verifier son existence et
  rediriger vers sa creation si besoin), versements successifs avec mise a
  jour automatique du solde.
- **Caisse** (`/api/caisse`) : ouverture et fermeture de session par
  declaration des coupures CEMAC (billets 10000/5000/2000/1000/500 + montant
  des pieces), calcul automatique du montant theorique et de l'ecart a la
  fermeture.
- **Credits clients** (`/api/credits`) : liste par statut, enregistrement des
  paiements avec mise a jour automatique du solde et du statut.
- **Depenses** (`/api/depenses`) : saisie et consultation par periode/boutique.
- **Approvisionnements** (`/api/approvisionnements`) : reception de stock
  aupres d'un fournisseur, mise a jour immediate du stock et du prix d'achat
  courant du produit.
- **Inventaire** (`/api/inventaires`) : comptage physique, calcul de l'ecart
  avec le stock theorique, ajustement automatique du stock.
- **Statistiques** (`/api/statistiques`) : chiffre d'affaires, nombre de
  ventes, benefice, nouveaux clients et produit le plus vendu, sur une
  periode libre ou un mois donne.
- **Boutiques, clients, fournisseurs, categories, marques** : CRUD classiques.
- **Utilisateurs** (`/api/utilisateurs`) : gestion des comptes (ADMIN),
  changement de mot de passe en self-service.

## Stack technique

Spring Boot 3.4, Spring Security (JWT via jjwt), Spring Data JPA, MySQL,
iText7 (generation PDF), Lombok, Bean Validation.
