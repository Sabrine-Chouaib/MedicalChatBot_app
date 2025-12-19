# Informations générales--
Nom de l'étudiant: Sabrine
Projet / Équipe: MedicalChatBot (équipe de 4)
Niche technology attribuée: Observabilité / Tracing distribué avec OpenTelemetry + Jaeger (Logging/Monitoring de pipeline RAG)
Date de début: 2025-12-10
Date de fin: 2025-12-18
# Description de la niche technology
Objectif / rôle dans le projet: Instrumenter le pipeline RAG du MedicalChatBot pour observer chaque étape du flux (PDF → chunking → embeddings → Pinecone → LLM), mesurer les performances, diagnostiquer les lenteurs et assurer la traçabilité des requêtes dans Jaeger.

Fonctionnalités principales utilisées:
-Tracing distribué: Spans hiérarchiques pour chaque étape métier et méthode technique.
-Attributs personnalisés: Mesures clés par span (taille texte, nombre de chunks, embedding.dim, topK…).
-Propagation du contexte: Spans parent-enfant pour visualiser la séquence complète du pipeline.
-Visualisation temps réel: Jaeger UI (timeline, tags, process, service & operation).

Intégration avec les core technologies:
-Spring Boot: Beans et configuration via RagConfig et injection des dépendances.
-OpenTelemetry (Java): Tracer pour créer les spans, export OTLP vers Jaeger.
-RAG components: PDFTextExtractor, MedicalSemanticChunker, MedicalEmbeddingsPipeline, PineconeStore, SystemPrompt, RagServiceLLM.
-Ollama: Génération d’embeddings et réponses LLM, tracées dans le pipeline.
-Pinecone: Similarité vectorielle, tracée avec topK et matches.

# Étapes d'intégration

1.	Définir le périmètre d’observabilité
      o	Étapes métier: PDF Extraction, Medical Chunking, Ollama Embedding, Pinecone Query, Ollama Generation, LLM.generateAnswer.
      o	Méthodes techniques: PDFTextExtractor.extractText, MedicalSemanticChunker.chunkMedicalText, MedicalEmbeddingsPipeline.generateEmbeddings, PineconeStore.query..

2.	Ajouter OpenTelemetry au projet
      o	Dépendances: OpenTelemetry SDK/Exporter (configuré via OpenTelemetryConfig).
      o	Tracer: Bean Tracer utilisé dans RagServiceLLM.

3.	Instrumenter le service RAG
      o	Spans parent/enfant: Chaque méthode crée un span; les étapes métier enveloppent les appels techniques.
      o	Attributs: Ajout de métriques clés sur chaque span.
      o	Propagation: Les spans sont liés au span racine “RAG Pipeline”.

4.	Configurer les Beans (RagConfig)
      o	Injection: Tracer,PDFTextExtractor, PDFChunker, PineconeStore, SystemPrompt, MedicalSemanticChunker, TextChunker.
      o	Ollama base URL: MedicalEmbeddingsPipeline.setOllamaBaseUrl(ollamaBaseUrl + embeddingsPath).
5.	Adapter le code aux nouveaux constructeurs
      o	RagServiceLLM: Constructeur avec 7 paramètres.
      o	Tests d’intégration: Mise à jour de FullPipelineRagWithLLMIT pour instancier toutes les dépendances.
6.	Lancer Jaeger et valider
      o	Docker: jaegertracing/all-in-one exposant l’UI sur 16686.
      o	Exécution, puis requêtes API et vérification des spans dans Jaeger.
## Défis rencontrés et solutions
Défi 1 : Lenteurs sur les embeddings Ollama → Les embeddings prenaient trop de temps (~79% du pipeline). 
*Solution : mesurer avec Jaeger et préparer optimisations (modèle plus rapide).
Défi 2 : Doubles spans métier/technique: Superposition incohérente des durées si les enfants ne reflètent pas le travail réel. → Risque de confusion avec trop de spans. 
*Solution : garder les deux niveaux mais hiérarchiser et normaliser les attributs.
Défi 3: Configuration de l’export OTLP et propagation de contexte 
Traces visibles mais chaînage incomplet (spans qui ne se relient pas), ou absence de certaines étapes (HTTP) dans la même trace.
Parfois, aucune trace si l’exporteur n’est pas joint correctement au SDK.
*solution: un bean unique Tracer, export OTLP correct et propagation du contexte dans les appels HTTP.

# Tests et validation:
-Tests unitaires:
      o	Validation du chunking sémantique (nombre de chunks, moyenne des longueurs).Chunking sémantique : j'ai validé que MedicalSemanticChunker.chunkMedicalText produit le bon nombre de chunks et que la moyenne des longueurs est cohérente.
      o	Validation du parsing JSON d’Ollama et Pinecone.

-Tests d’intégration:
      o	FullPipelineRagWithLLMIT: Exécution du pipeline complet avec spans visibles dans Jaeger.
      o	Vérification des tags ajoutés (pdf.text.length, chunk.count, embedding.dim, pinecone.matches.count, llm.response.length).
## Résultats et captures d'écran :
![img_7.png](img_7.png)
![img_6.png](img_6.png)

## Documentation complémentaire

Liens vers guides, manuels, documentation officielle:
https://docs.spring.io/spring-boot/docs/current/reference/html/ – pour comprendre la configuration des beans et du cycle de vie.

https://opentelemetry.io/docs/instrumentation/java/ – pour la création de spans, configuration du Tracer et export OTLP.

https://www.jaegertracing.io/docs/ – pour la visualisation des traces et l’utilisation des dashboards.
Notes et remarques personnelles :
Bénéfices du projet :

J’ai appris à instrumenter un pipeline RAG avec OpenTelemetry et Jaeger, ce qui m’a permis de comprendre en profondeur la différence entre spans métier et technique.
J’ai renforcé mes compétences en Spring Boot (beans, injection de dépendances) et en intégration de services externes (Ollama, Pinecone, PDFBox).
J’ai acquis une maîtrise pratique de l’observabilité, en identifiant les goulots d’étranglement (embeddings) et en normalisant les attributs pour une lecture claire dans Jaeger.
Ce projet m’a donné une expérience complète de bout en bout : configuration Maven, gestion des erreurs, intégration des librairies, et documentation académique.
Perspectives :
Optimiser les embeddings avec batching, cache et modèles plus rapides, afin de réduire la latence du pipeline.