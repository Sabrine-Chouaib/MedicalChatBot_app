## Informations générales
- Nom de l'étudiant : Amara Eya
- Projet / Équipe : Medical ChatBot
- Niche technology attribuée : Docker Compose
- Date de début : 11/12/2025
- Date de fin : 18/12/2025

## Description de la niche technology
- Objectif / rôle dans le projet :
  Docker Compose est utilisé pour orchestrer et gérer plusieurs conteneurs dans le projet MedicalChatBot.  
  Il permet de lancer et de coordonner le backend Spring Boot, la base de données PostgreSQL, le serveur Ollama (LLM), et le module Jaeger (monitoring) avec une seule commande.

- Fonctionnalités principales utilisées :
    - Définition des services (backend, postgres, ollama, jaeger).
    - Gestion des réseaux inter-containers pour assurer la communication entre les services.
    - Volumes persistants pour PostgreSQL et Ollama afin de conserver les données et les modèles.
    - Variables d’environnement pour configurer les services (DB, JWT, Pinecone, LLM API).
    - Ajout d’un service Jaeger pour le monitoring et le tracing distribué.

- Intégration avec les core technologies :
    - Spring Boot (backend) se connecte à PostgreSQL via l’URL définie dans `docker-compose.yml`.
    - Ollama est exposé sur le port 11434 et utilisé par le backend pour générer des réponses.
    - Pinecone est intégré via des variables d’environnement pour la recherche vectorielle.
    - Jaeger est intégré via Docker Compose pour observer et tracer les requêtes du backend.

## Étapes d'intégration
1. Étape 1 : Création du fichier `docker-compose.yml` avec définition des services principaux (postgres, backend, ollama).
2. Étape 2 : Ajout du service Jaeger (`jaegertracing/all-in-one`) pour le monitoring.
3. Étape 3 : Configuration des ports Jaeger (16686 pour UI, 4317/4318 pour OTLP).
4. Étape 4 : Ajout des volumes pour persister les données PostgreSQL et les modèles Ollama.
5. Étape 5 : Configuration des variables d’environnement pour chaque service (DB credentials, JWT secret, API keys).
6. Étape 6 : Mise en place d’un healthcheck pour Ollama afin d’assurer que le backend démarre seulement quand Ollama est prêt.
7. Étape 7 : Vérification avec `docker compose up` et accès à l’application via `localhost:8081` et Jaeger via `localhost:16686`.

## Défis rencontrés et solutions
- Défi 1 : Les modèles Ollama étaient installés sur Windows mais absents dans le conteneur.
    - Solution : Montage du volume `C:/Users/user/.ollama:/root/.ollama` pour partager les modèles entre l’hôte et le conteneur.

- Défi 2 : Erreurs de connexion (`ConnectException`) entre backend et Ollama.
    - Solution : Utilisation du nom de service Docker (`http://ollama:11434`) au lieu de `localhost`.

- Défi 3 : Problème avec `ollama-init` (absence de `curl/wget`).
    - Solution : Simplification du `docker-compose.yml` en supprimant `ollama-init` et en utilisant un healthcheck.

- Défi 4 : Intégration de Jaeger dans Docker Compose.
    - Solution : Ajout du service Jaeger avec l’image `jaegertracing/all-in-one` et configuration des ports nécessaires.

## Tests et validation
- Tests unitaires :
    - Vérification des classes de service Spring Boot (authentification JWT, RAG pipeline).
- Tests d'intégration :
    - Lancement complet du stack avec `docker compose up`.
    - Vérification que le backend peut interroger PostgreSQL et Ollama.
    - Vérification que Jaeger est accessible via `localhost:16686`.
- Résultats et captures d'écran :
    - Interface web accessible sur `localhost:8081`.
    - Réponses générées par le modèle `phi2:latest`.
    - Traces visibles dans Jaeger (requêtes backend → DB → Ollama).

## Documentation complémentaire
- Liens vers guides, manuels, documentation officielle :
    - [Documentation Docker Compose](https://docs.docker.com/compose/)
- Notes et remarques personnelles :
    - Docker Compose simplifie énormément le déploiement multi-services.
    - Le montage de volumes est essentiel pour éviter la perte de données et partager les modèles entre hôte et conteneur.
    - L’ajout de Jaeger via Docker Compose permet de surveiller facilement les performances et les traces du système.