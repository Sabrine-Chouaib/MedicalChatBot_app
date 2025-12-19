# Informations générales

Projet : MedicalChatBot

Technologies principales : Spring Boot, PostgreSQL, Ollama (LLM), Jaeger (monitoring), Docker Compose

Objectif : Développer un chatbot médical intelligent capable de répondre à des questions médicales en utilisant un modèle de langage et une base de données.

# Architecture du projet

User[Utilisateur] -->|HTTP Request| Backend[Spring Boot Backend]
Backend -->|JPA Queries| Database[(PostgreSQL)]
Backend -->|API Calls| Ollama[LLM Server]
Backend -->|Tracing| Jaeger[Monitoring]
Ollama --> Models[LLM Models: phi2, nomic-embed-text]

# Guide d’installation

### 1. Prérequis

Docker Desktop installé sur Windows.

Git pour cloner le projet.

Compte Docker Hub (si CI/CD avec GitHub Actions).

### 2.Cloner le projet

git clone https://github.com/<ton_repo>/MedicalChatBot_app.git
cd MedicalChatBot_app

### 3.Vérifier le Dockerfile

Le backend est construit en deux étapes :

Build stage : compilation avec Maven.

Runtime stage : exécution avec JDK 17 Alpine.

dockerfile
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

### 4.Lancer avec Docker Compose


docker compose up --build -d

### 5.Accéder aux services

- Backend API : http://localhost:8081
- PostgreSQL : localhost:5432
- Ollama API : http://localhost:11434
- Jaeger UI : http://localhost:16686

### 6.Tests et validation

Vérifier que le backend démarre correctement (docker compose logs backend).

-  Tester une requête API :


curl http://localhost:8081/api/medical?question=What is diabetes?
Vérifier dans Jaeger que les traces apparaissent.

- Vérifier que les modèles Ollama (phi2:latest, nomic-embed-text:latest) sont bien listés :


docker exec -it medical_ollama ollama list