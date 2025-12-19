# NicheIntegration.md

## Informations générales
- **Nom de l'étudiant** : Ayda Ben Abdelhafidh  
- **Projet / Équipe** : Assistant Médical Intelligent – Projet  
- **Niche technology attribuée** : Intelligence Artificielle – Chatbot médical (RAG + LLM local) : 
- **Date de début** : 01/12/2025
- **Date de fin** :   18/12/2025

---

## Description de la niche technology

### Objectif / rôle dans le projet

L’objectif de cette niche technologique est de concevoir et intégrer un **assistant médical intelligent** capable de répondre de manière pertinente et structurée à des questions médicales, en s’appuyant sur :
- des documents médicaux indexés (base vectorielle) : Pinecone
- un modèle de langage local (LLM) : Phi2-local
- une architecture **RAG (Retrieval Augmented Generation)**.

Cette technologie permet d’améliorer la fiabilité des réponses en combinant **recherche sémantique** et **génération de texte**.

---

### Fonctionnalités principales utilisées

- Implémentation de l’architecture RAG : (question → recherche → contexte → génération)
- Génération d’embeddings médicaux
- Recherche sémantique via une base vectorielle Pinecone
- Construction dynamique des prompts
- Génération de réponses médicales via un LLM local (Ollama) (Ollama – Phi2)
- Backend Spring Boot (API REST) pour exposer le moteur IA
- Frontend web interactif (HTML / CSS / JavaScript)



---

### Intégration avec les core technologies

- **Backend** : Java – Spring Boot
- **LLM** : Ollama (modèle local)
- **Vector Store** : Pinecone (embeddings médicaux)
- **Frontend** : HTML, CSS moderne, JavaScript vanilla
- **Communication** : API REST (JSON)

L’intégration permet une communication fluide entre le frontend et le backend via une API `/api/chat`.

---

## Étapes d’intégration

1. **Analyse du besoin**  
   Définition du cas d’usage : chatbot médical fiable et intuitif qui génére de réponses médicales fiables et contextualisées.

2. **Mise en place du backend**  
   - Création du projet Spring Boot
   - Configuration des services RAG
   - Connexion à Pinecone
   - Connexion à Ollama

3. **Implémentation de la logique RAG** 
   - Préparation des documents médicaux : extraction du texte depuis le pdf et chunking (segmentation) 
   - Génération des embeddings : Transformation des textes en vecteurs numériques.
   - Stockage dans Pinecone

   - Recherche sémantique des documents pertinents : Comparaison question ↔ documents.
   - Construction du prompt système ( Transmission du contexte vers le LLM ) 
 

4. **Création de l’API REST**  
   - Endpoint POST `/api/chat`
   - Gestion des erreurs

5. **Développement du frontend**  
   - Interface type ChatGPT
   

6. **Tests et validation**  
   - Tests avec Postman
   - Tests fonctionnels complets

---

## Défis rencontrés et solutions

### Défi 1 : Erreurs HTTP avec Ollama (405)
- **Problème** : Mauvaise méthode HTTP utilisée
- **Solution** : Correction de l’appel API vers la bonne route POST Ollama

### Défi 2 : Mauvaise configuration Pinecone
- **Problème** : Variables d’environnement non reconnues
- **Solution** : Centralisation dans `application.properties`

### Défi 3 : Réponses longues et peu lisibles
- **Problème** : Mélange de langues et détails inutiles
- **Solution** : Amélioration du prompt système pour des réponses claires, 

### Défi 4 : UX peu intuitive
- **Problème** : Interface basique
- **Solution** : Design moderne, message d’accueil centré, animations et typing effect

---

## Tests et validation

- **Tests unitaires** : Vérification des services backend : Extraction -chunking - Embeddings - Stockage dans Pinecone 
- **Tests d’intégration** : API testée avec Postman
- **Tests fonctionnels** : Scénarios utilisateurs réels
- **Résultats** :
  - Réponses cohérentes
  - Temps de réponse satisfaisant
  - Interface fluide et intuitive

---

## Documentation complémentaire

- Documentation Spring Boot   : https://docs.spring.io/spring-boot/index.html
- Documentation Pinecone   :  https://docs.pinecone.io/guides/get-started/overview
- Documentation Ollama     : https://docs.ollama.com/
- Ressources sur les architectures RAG  : https://www.redhat.com/fr/topics/ai/what-is-retrieval-augmented-generation

### Notes et remarques personnelles

Ce projet m’a permis de :
- Comprendre en profondeur l’architecture RAG
- Intégrer un LLM local dans une application réelle
- Améliorer mes compétences en Java backend 


---

**Projet validé et fonctionnel – Assistant Médical Intelligent**

