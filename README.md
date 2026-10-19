# TaskFlow

TaskFlow est une application web de gestion de tâches à plusieurs utilisateurs. Chacun se connecte avec son compte, crée ses tâches, les suit jusqu'à leur terme et ne voit jamais celles des autres. Un administrateur dispose en plus d'une vue globale pour superviser et nettoyer les tâches.

Le projet est découpé en trois briques : une interface Vue 3 en TypeScript, une API REST Spring Boot 3, et un serveur Keycloak qui gère toute l'authentification. Les données sont stockées dans PostgreSQL. L'ensemble tourne dans Docker, est testé par GitHub Actions et hébergé sur Render.

## Essayer l'application

L'application est en ligne : ouvrez le lien du frontend Vue.js et connectez-vous avec l'un des comptes ci-dessous.

| Service | Adresse |
|---|---|
| Frontend (Vue.js) | https://taskflowvuejs.onrender.com |
| API (Spring Boot) | https://taskflow-av7e.onrender.com |
| État de l'API | https://taskflow-av7e.onrender.com/actuator/health |
| Documentation Swagger | https://taskflow-av7e.onrender.com/swagger-ui/index.html |
| Keycloak | https://taskflow-keycloak-038u.onrender.com |

**Comptes de test**

| Utilisateur | Mot de passe | Rôles |
|---|---|---|
| `alice` | `alice` | user |
| `bob` | `bob` | user, admin |

Le compte `bob` voit en plus l'espace « Admin ».

Une requête directe à l'API sans jeton renvoie `401`, c'est voulu : toute l'API est protégée. Passez par l'application pour la tester.

Attention, l'hébergement est gratuit : après quelques minutes d'inactivité les services se mettent en veille, et le premier chargement peut durer une minute environ. La base de données gratuite de Render est aussi supprimée au bout de 30 jours.

## Ce que permet l'application

Pour un utilisateur :

- se connecter et se déconnecter via Keycloak, consulter son profil ;
- créer, modifier et supprimer une tâche (titre, description, priorité basse, normale ou haute, date d'échéance) ;
- terminer une tâche ou la rouvrir, avec un indicateur clair « en cours » ou « terminée » et un repère pour les retards ;
- filtrer par statut ou priorité, trier par échéance, parcourir les pages de résultats ;
- suivre l'essentiel sur un tableau de bord : tâches terminées, en cours et en retard.

Pour un administrateur : consulter les tâches de tous les utilisateurs, en supprimer, et voir des statistiques par utilisateur. Cet espace est refusé à tout compte sans le rôle `admin`, côté interface comme côté API.

## Comment ça fonctionne

1. Le navigateur charge l'application Vue, qui redirige l'utilisateur vers Keycloak s'il n'est pas connecté.
2. Après la saisie du mot de passe, Keycloak renvoie l'utilisateur vers l'application avec un jeton signé (flux OpenID Connect avec PKCE).
3. À chaque appel, l'application ajoute ce jeton à la requête envoyée à l'API.
4. L'API vérifie la signature et l'émetteur du jeton, identifie l'utilisateur et ne lui renvoie que ses propres tâches.

Le mot de passe ne transite donc jamais par l'API ni par l'application, seulement par Keycloak.

## Organisation du dépôt

- `TaskFlowVueJS/` : l'interface (Vue 3, Vue Router, keycloak-js, Vite, Vitest).
- `taskflowSpring/` : l'API (Spring Boot 3, Spring Security, Flyway, Swagger).
- `keycloak/` : l'image Keycloak avec le realm `taskflow` importé automatiquement au démarrage.
- `docker-compose.yml` : lance toute la pile en local.
- `render.yaml` : décrit l'infrastructure déployée sur Render.
- `.github/workflows/` : l'intégration continue.

## Démarrage en local

Il faut Docker Desktop. Pour travailler sur un projet isolément, il faut aussi Java 17 ou plus (avec Maven) pour l'API et Node.js 24 pour l'interface.

```bash
cp .env.example .env        # sous PowerShell : Copy-Item .env.example .env
docker compose up --build
```

Quand tout est démarré :

- l'application répond sur http://localhost:5173 ;
- l'API sur http://localhost:8081 (état : `/actuator/health`) ;
- Keycloak sur http://localhost:8180.

Le realm et les comptes de test sont créés automatiquement. Pour arrêter : `docker compose down`.

## Configuration

Le code ne contient aucune adresse ni aucun secret. Tout est lu dans des variables d'environnement. En local, elles viennent du fichier `.env`, copié depuis `.env.example` et jamais versionné (pensez à y changer les mots de passe). Sur Render, elles sont saisies dans le tableau de bord.

Les principales :

- base de données : `DB_NAME`, `DB_URL`, `DB_USER`, `DB_PASSWORD` ;
- Keycloak : `KEYCLOAK_ADMIN_USER`, `KEYCLOAK_ADMIN_PASSWORD`, et pour l'API `KEYCLOAK_ISSUER_URI` (l'adresse complète du realm, qui doit se terminer par `/realms/taskflow`) ;
- autorisations d'accès : `CORS_ALLOWED_ORIGINS` (l'adresse de l'interface) et `PORT` (le port de l'API) ;
- interface, lues au moment de la compilation : `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, `VITE_KEYCLOAK_CLIENT_ID`, `VITE_API_URL`.

Pour démarrer l'API depuis IntelliJ, ajoutez `DB_URL`, `DB_USER`, `DB_PASSWORD`, `KEYCLOAK_ISSUER_URI` et `CORS_ALLOWED_ORIGINS` dans la configuration d'exécution de la classe `com.taskflow.TaskflowSpringApplication`.

## Tests

```bash
cd taskflowSpring && mvn verify                                 # API
cd TaskFlowVueJS && npm ci && npm run build && npm test        # interface
```

À chaque envoi de code sur GitHub, le workflow dans `.github/workflows/` compile les deux projets et exécute les tests.

## Déploiement sur Render

Le fichier `render.yaml` crée d'un seul coup la base PostgreSQL, Keycloak, l'API et l'interface, tous en HTTPS. Les secrets ne sont jamais dans le dépôt : ils sont fournis par Render ou saisis à la main dans son tableau de bord.

Le déploiement se déclenche automatiquement sur la branche principale, et seulement lorsque les tests passent.
